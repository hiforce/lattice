package org.hiforce.lattice.runtime.ability.execute;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.business.ITemplate;
import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.hifforce.lattice.model.ability.execute.Reducer;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
@SuppressWarnings("rawtypes")
@Slf4j
public class RunnerCollection<ExtensionPoints, R> {

    public static final Predicate ACCEPT_ALL = o -> true;
    public static final Producer PRODUCE_NULL = () -> null;

    private IBizObject bizInstance;

    private List<RunnerItemEntry<ExtensionPoints, R>> runnerList = Collections.emptyList();
    private Predicate<RunnerItemEntry<ExtensionPoints, R>> predicate = ACCEPT_ALL;
    private Producer<ExtensionPoints, R> defaultProducer = PRODUCE_NULL;

    private RunnerCollection<ExtensionPoints, R> parent;

    private List<InstantItem> finalColl;

    private boolean loadBizExt;

    private boolean loadDefaultExtension;

    private RunnerCollection() {
    }

    static <ExtensionPoints, R> RunnerCollection<ExtensionPoints, R> of(
            IBizObject bizInstance,
            List<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>> runnerList,
            Predicate<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>> predicate,
            Producer<ExtensionPoints, R> defaultResult, boolean loadBizExt, boolean loadDefaultExtension) {

        RunnerCollection<ExtensionPoints, R> runnerCollection = new RunnerCollection<>();
        runnerCollection.bizInstance = bizInstance;
        runnerCollection.runnerList = runnerList;
        runnerCollection.predicate = predicate;
        if (defaultResult != null) {
            runnerCollection.defaultProducer = defaultResult;
        }
        runnerCollection.loadBizExt = loadBizExt;
        runnerCollection.loadDefaultExtension = loadDefaultExtension;
        return runnerCollection;
    }

    public static <ExtensionPoints, R> RunnerCollection<ExtensionPoints, R> of(
            IBizObject bizInstance,
            List<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>> runnerList,
            Predicate<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>> predicate) {
        return of(bizInstance, runnerList, predicate, null, false, false);
    }

    public static <ExtensionPoints, R> RunnerCollection<ExtensionPoints, R> combine(
            RunnerCollection<ExtensionPoints, R> runnerCollection, Producer<ExtensionPoints, R> producer, boolean loadBizExt, boolean loadDefaultExtension) {
        RunnerCollection<ExtensionPoints, R> runnerResult = new RunnerCollection<>();
        runnerResult.parent = runnerCollection;
        runnerResult.bizInstance = runnerCollection.bizInstance;
        if (producer != null) {
            runnerResult.defaultProducer = producer;
        }
        runnerResult.loadBizExt = loadBizExt;
        runnerResult.loadDefaultExtension = loadDefaultExtension;
        return runnerResult;
    }

    static <ExtensionPoints, R> RunnerCollection<ExtensionPoints, R> combine(RunnerCollection<ExtensionPoints, R> runnerCollection, RunnerCollection<ExtensionPoints, R> runnerCollection2) {
        runnerCollection2.parent = runnerCollection;
        return runnerCollection2;
    }

    static <ExtensionPoints, R> RunnerCollection<ExtensionPoints, R> newEmptyCollection() {
        return new RunnerCollection<>();
    }

    RunnerCollection<ExtensionPoints, R> merge(RunnerCollection<ExtensionPoints, R> runnerCollection, IBizObject bizInstance) {
        RunnerCollection<ExtensionPoints, R> parent = this.parent;
        if (parent != null) {
            runnerCollection.parent = parent;
            this.parent = runnerCollection;
        } else {
            this.parent = runnerCollection;
        }
        runnerCollection.withBizInstance(bizInstance);
        return this;
    }

    // set bizInstance for RunnerCollections create which the bizInstance is null
    RunnerCollection<ExtensionPoints, R> withBizInstance(IBizObject bizInstance) {
        this.bizInstance = bizInstance;
        RunnerCollection<ExtensionPoints, R> parent = this.parent;
        while (parent != null && parent.bizInstance == null) {
            parent.bizInstance = bizInstance;
            parent = parent.parent;
        }
        return this;
    }

    private <T> List<InstantItem<ExtensionPoints, T>> generateInstantItem() {
        List result = this.finalColl;
        if (result == null) {
            result = new ArrayList<>(32);
            this.collect(result);
            this.finalColl = result;
        }
        return result;
    }

    private void collect(List<InstantItem> result) {
        RunnerCollection<ExtensionPoints, R> parent = this.parent;
        if (parent != null) {
            parent.collect(result);
        }
        boolean skipDefault = false;
        IBizObject bizInstance = this.bizInstance;
        List<RunnerItemEntry<ExtensionPoints, R>> runnerList = this.runnerList;
        if (runnerList != null) {
            Predicate<RunnerItemEntry<ExtensionPoints, R>> predicate = this.predicate;
            for (RunnerItemEntry<ExtensionPoints, R> item : this.runnerList) {
                boolean t = predicate.test(item);
                skipDefault |= t;
                if (t) {
                    result.add(new InstantItem<>(item, bizInstance));
                }
            }
            if (!skipDefault && this.loadBizExt) {
                RunnerItemEntry<ExtensionPoints, R> defaultItem = this.defaultProducer.produce();
                if (defaultItem != null) {
                    result.add(new InstantItem<>(defaultItem, bizInstance));
                }
            }
        }
    }

    private void updateResult(List result) {
        this.finalColl = result;
    }

    RunnerCollection<ExtensionPoints, R> sort() {
        List<InstantItem<ExtensionPoints, R>> result = this.generateInstantItem();
        result.sort(Comparator.comparingInt(p -> p.runnerItemEntry.extensionRunner.getPriority()));
        return this;
    }

    public RunnerCollection<ExtensionPoints, R> distinct() {
        List<InstantItem<ExtensionPoints, R>> result = this.generateInstantItem();
        result = distinctRunners(result);
        this.updateResult(result);
        return this;
    }

    private List<InstantItem<ExtensionPoints, R>> distinctRunners(List<InstantItem<ExtensionPoints, R>> runners) {
        List<InstantItem<ExtensionPoints, R>> output = new ArrayList<>(runners.size());
        Map<ExtensionRunnerType, Set<String>> map = Maps.newHashMap();

        for (InstantItem<ExtensionPoints, R> runner : runners) {
            if (null == runner.runnerItemEntry.template) {
                output.add(runner);
                continue;
            }
            Set<String> set = map.computeIfAbsent(runner.runnerItemEntry.getRunnerType(), k -> Sets.newHashSet());
            boolean newAdd = set.add(runner.runnerItemEntry.template.getCode());
            if (newAdd) {
                output.add(runner);
            }
        }
        return output;
    }

    public <T, R> ExecuteResult<R> reduceExecute(Reducer<T, R> reducer, ExtensionCallback<IBusinessExt, T> callback, List<T> results) {
        List<InstantItem<ExtensionPoints, T>> list = this.generateInstantItem();
        if (list.isEmpty()) {
            return ExecuteResult.success(reducer.reduce(results), null, null);
        }

        List<ExtensionRunner.CollectionRunnerExecuteResult> executeResults = new ArrayList<>(list.size() * 2);// make extra room for multi-results
        for (InstantItem<ExtensionPoints, T> item : list) {
            ExtensionRunner.CollectionRunnerExecuteResult executeResult = new ExtensionRunner.CollectionRunnerExecuteResult();
            List<T> itemResult = item.runAllMatched(callback, executeResult);
            executeResult.setResults(itemResult);
            executeResults.add(executeResult);
            if (reducer.willBreak(itemResult)) {
                return ExecuteResult.success(reducer.reduce(itemResult), convertToTemplateList(list), executeResults);
            } else {
                if (itemResult.size() == 1) {
                    results.add(itemResult.get(0));
                } else {
                    results.addAll(itemResult);
                }
            }
        }
        return ExecuteResult.success(reducer.reduce(results), convertToTemplateList(list), executeResults);
    }

    private <T> List<ITemplate> convertToTemplateList(List<InstantItem<ExtensionPoints, T>> list) {
        List<ITemplate> templates = new ArrayList<>(list.size());// make extra room for multi-results
        list.forEach(p -> templates.add(p.runnerItemEntry.template));
        return templates;
    }

    public static class RunnerItemEntry<ExtensionPoints, R> {
        @Getter
        ITemplate template;
        ExtensionRunner<ExtensionPoints, R> extensionRunner;
        Object abilityInstance;

        public ExtensionRunnerType getRunnerType() {
            return extensionRunner.getType();
        }

        public RunnerItemEntry(ITemplate template, ExtensionRunner<ExtensionPoints, R> extensionRunner, Object abilityInstance) {
            this.template = template;
            this.extensionRunner = extensionRunner;
            this.abilityInstance = abilityInstance;
        }

        @Override
        public String toString() {
            return "[" + (template != null ? template.getCode() : null) + "|"
                    + (extensionRunner.getModel() != null ? extensionRunner.getModel().getClass().getName() : null) + "]";
        }
    }

    public interface Producer<ExtensionPoints, R> {
        RunnerCollection.RunnerItemEntry<ExtensionPoints, R> produce();
    }

    private static class InstantItem<ExtensionPoints, R> {
        RunnerItemEntry<ExtensionPoints, R> runnerItemEntry;
        IBizObject bizInstance;

        public InstantItem(RunnerItemEntry<ExtensionPoints, R> runnerItemEntry, IBizObject bizInstance) {
            this.runnerItemEntry = runnerItemEntry;
            this.bizInstance = bizInstance;
        }

        @SuppressWarnings("unchecked")
        public List<R> runAllMatched(ExtensionCallback<IBusinessExt, R> callback, ExtensionRunner.RunnerExecuteResult executeResult) {
            RunnerItemEntry<ExtensionPoints, R> entry = this.runnerItemEntry;
            try {
                return entry.extensionRunner.runAllMatched(entry.abilityInstance, this.bizInstance, callback, executeResult);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw ex;
            }
        }
    }
}
