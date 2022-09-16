package org.hifforce.lattice.annotation.parser.impl;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.annotation.ScanSkip;
import org.hifforce.lattice.annotation.parser.ScanSkipAnnotationParser;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@AutoService(ScanSkipAnnotationParser.class)
public class DefaultScanSkipAnnotationParser extends ScanSkipAnnotationParser<ScanSkip> {
    @Override
    public Class<ScanSkip> getAnnotationClass() {
        return ScanSkip.class;
    }
}