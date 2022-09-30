package org.hiforce.lattice.runtime.ability.execute;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.extension.ExtensionRunner;
import org.hifforce.lattice.extension.ExtensionRunnerType;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.hifforce.lattice.model.ability.execute.Reducer;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.register.TemplateSpec;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
@SuppressWarnings("all")
@Slf4j
public class RunnerCollection<ExtensionPoints, R> {

    public static final Predicate ACCEPT_ALL = o -> true;
    public static final Producer PRODUCE_NULL = () -> null;

    private IBizObject bizInstance;

    private List<RunnerItemEntry<R>> runnerList = Collections.emptyList();
    private Predicate<RunnerItemEntry<R>> predicate = ACCEPT_ALL;
    private Producer<ExtensionPoints, R> defaultProducer = PRODUCE_NULL;

    private RunnerCollection<ExtensionPoints, R> parent;

    private List<InstantItem> finalColl;

    private boolean loadBizExt;

    private boolean loadDefaultExtension;

    private RunnerCollection() {
    }

    public static <ExtensionPoints, R> RunnerCollection<ExtensionPoints, R> of(
            IBizObject bizInstance,
            List<RunnerCollection.RunnerItemEntry<R>> runnerList,
            Predicate<RunnerCollection.RunnerItemEntry<R>> predicate,
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
            List<RunnerCollection.RunnerItemEntry<R>> runnerList,
            Predicate<RunnerCollection.RunnerItemEntry<R>> predicate) {
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

    public static <ExtensionPoints, R> RunnerCollection<ExtensionPoints, R> combine(RunnerCollection<ExtensionPoints, R> runnerCollection, RunnerCollection<ExtensionPoints, R> runnerCollection2) {
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
        List<RunnerItemEntry<R>> runnerList = this.runnerList;
        if (runnerList != null) {
            Predicate<RunnerItemEntry<R>> predicate = this.predicate;
            for (RunnerItemEntry<R> item : this.runnerList) {
                boolean t = predicate.test(item);
                skipDefault |= t;
                if (t) {
                    result.add(new InstantItem<>(item, bizInstance));
                }
            }
            if (!skipDefault && this.loadBizExt) {
                RunnerItemEntry<R> defaultItem = this.defaultProducer.produce();
                if (defaultItem != null) {
                    result.add(new InstantItem<>(defaultItem, bizInstance));
                }
            }
        }
    }

    private void updateResult(List result) {
        this.finalColl = result;
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

    public <T, R> ExecuteResult<R> reduceExecute(String extCode, Reducer<T, R> reducer, ExtensionCallback<IBusinessExt, T> callback, List<T> results) {
        List<InstantItem<ExtensionPoints, T>> list = this.generateInstantItem();
        if (list.isEmpty()) {
            return ExecuteResult.success(extCode, reducer.reduceName(), reducer.reduce(results), null, null);
        }

        List<ExtensionRunner.CollectionRunnerExecuteResult> executeResults = new ArrayList<>(list.size() * 2);
        for (InstantItem<ExtensionPoints, T> item : list) {
            ExtensionRunner.CollectionRunnerExecuteResult executeResult = new ExtensionRunner.CollectionRunnerExecuteResult();
            List<T> itemResult = item.runAllMatched(callback, executeResult);
            executeResult.setResults(itemResult);
            executeResults.add(executeResult);
            if (reducer.willBreak(itemResult)) {
                return ExecuteResult.success(extCode, reducer.reduceName(),
                        reducer.reduce(itemResult), convertToTemplateList(list), executeResults);
            } else {
                if (itemResult.size() == 1) {
                    results.add(itemResult.get(0));
                } else {
                    results.addAll(itemResult);
                }
            }
        }
        return ExecuteResult.success(extCode, reducer.reduceName(),
                reducer.reduce(results), convertToTemplateList(list), executeResults);
    }

    private <T> List<TemplateSpec> convertToTemplateList(List<InstantItem<ExtensionPoints, T>> list) {
        List<TemplateSpec> templates = new ArrayList<>(list.size());
        list.forEach(p -> templates.add(p.runnerItemEntry.template));
        return templates;
    }

    public static class RunnerItemEntry<R> {

        @Getter
        TemplateSpec template;

        ExtensionRunner<R> extensionRunner;

        public ExtensionRunnerType getRunnerType() {
            return extensionRunner.getType();
        }

        public RunnerItemEntry(TemplateSpec template, ExtensionRunner<R> extensionRunner) {
            this.template = template;
            this.extensionRunner = extensionRunner;
        }

        @Override
        public String toString() {
            return "[" + template.getCode() + "|"
                    + (extensionRunner.getModel() != null ? extensionRunner.getModel().getClass().getName() : null) + "]";
        }
    }

    public interface Producer<ExtensionPoints, R> {
        RunnerCollection.RunnerItemEntry<R> produce();
    }

    private class InstantItem<ExtensionPoints, R> {
        RunnerItemEntry<R> runnerItemEntry;
        IBizObject bizObject;

        public InstantItem(RunnerItemEntry<R> runnerItemEntry, IBizObject bizObject) {
            this.runnerItemEntry = runnerItemEntry;
            this.bizObject = bizObject;
        }

        @SuppressWarnings("unchecked")
        public List<R> runAllMatched(
                ExtensionCallback<IBusinessExt, R> callback, ExtensionRunner.RunnerExecuteResult result) {
            RunnerItemEntry<R> entry = this.runnerItemEntry;
            try {
                return entry.extensionRunner.runAllMatched(this.bizObject, callback, result);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw ex;
            }
        }
    }
}
