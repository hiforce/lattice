package org.hiforce.lattice.maven.builder;

import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class RealizationInfoBuilder extends LatticeInfoBuilder {

    public RealizationInfoBuilder(LatticeBuildPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getSpiClassName() {
        return IBusinessExt.class.getName();
    }

    @Override
    public void build() {
        List<String> classNames = getImportInfoClassNames();
        classNames.addAll(getProvidedInfoClassNames());
        TemplateRegister.getInstance().registerRealizations(loadTargetClassList(classNames));
    }
}
