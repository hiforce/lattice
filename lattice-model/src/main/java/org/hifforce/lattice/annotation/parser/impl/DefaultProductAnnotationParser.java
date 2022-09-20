package org.hifforce.lattice.annotation.parser.impl;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.annotation.Product;
import org.hifforce.lattice.annotation.parser.ProductAnnotationParser;
import org.hifforce.lattice.model.business.TemplateType;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@AutoService(ProductAnnotationParser.class)
public class DefaultProductAnnotationParser extends ProductAnnotationParser<Product> {
    @Override
    public Class<Product> getAnnotationClass() {
        return Product.class;
    }

    @Override
    public String getCode(Product annotation) {
        return annotation.code();
    }

    @Override
    public String getName(Product annotation) {
        return annotation.name();
    }

    @Override
    public String getDesc(Product annotation) {
        return annotation.desc();
    }

    @Override
    public TemplateType getType(Product annotation) {
        return annotation.type();
    }

    @Override
    public int getPriority(Product annotation) {
        return annotation.priority();
    }
}
