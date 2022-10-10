package org.hiforce.lattice.model.ability;

import lombok.NonNull;
import org.hiforce.lattice.annotation.ScanSkip;

import java.util.List;

/**
 * The interface for Business Extension's class.
 *
 * @author Rocky Yu
 * @since 2022/9/15
 */
public interface IBusinessExt {

    /**
     * ind the Sub-BusinessExt facade via the extension's code in specific scenario.
     *
     * @param extCode  the code of the extension point.
     * @param scenario the specific scenario.
     * @return the found Sub-BusinessExt facade realization.
     */
    @ScanSkip
    IBusinessExt getBusinessExtByCode(String extCode, String scenario);

    /**
     * @return All the sub business extension facades.
     */
    @NonNull
    @ScanSkip
    List<IBusinessExt> getAllSubBusinessExt();
}
