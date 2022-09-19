package org.hiforce.lattice.runtime.cache;

import lombok.Getter;
import org.hiforce.lattice.runtime.cache.key.BaseObjectCacheKey;

import javax.annotation.Nonnull;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class ExtensionRunnerCacheKey extends BaseObjectCacheKey {

	@Getter
	@Nonnull
	String extensionCode;

	@Getter
	@Nonnull
	String bizCode;

	@Getter
	String scenario;

	@Getter
	boolean supportCustomization;

	@Getter
	boolean onlyProduct;


	public ExtensionRunnerCacheKey(@Nonnull String extensionCode, @Nonnull String bizCode, String scenario, boolean supportCustomization, boolean onlyProduct) {
		this.extensionCode = extensionCode;
		this.bizCode = bizCode;
		this.scenario = scenario;
		this.supportCustomization = supportCustomization;
		this.onlyProduct = onlyProduct;
		buildObjectCacheUniqueId();
	}

	private int hashCode;


	@Override
	public boolean customEquals(Object o) {
		ExtensionRunnerCacheKey that = (ExtensionRunnerCacheKey) o;

		if (supportCustomization != that.supportCustomization) return false;
		if (onlyProduct != that.onlyProduct) return false;
		if (!extensionCode.equals(that.extensionCode))
			return false;
		if (!bizCode.equals(that.bizCode)) return false;
		return scenario != null ? scenario.equals(that.scenario) : that.scenario == null;
	}

	@Override
	public int hashCode() {
		if (this.hashCode > 0) {
			return this.hashCode;
		}
		int result = extensionCode.hashCode();
		result = 31 * result + bizCode.hashCode();
		result = 31 * result + (scenario != null ? scenario.hashCode() : 0);
		result = 31 * result + (supportCustomization ? 1 : 0);
		result = 31 * result + (onlyProduct ? 1 : 0);
		this.hashCode = result;
		return result;
	}

	@Override
	public String getTemplateCode() {
		return null;
	}

	@Override
	public boolean validateIndex() {
		if (null == getBizCodeIndex() || null == getExtCodeIndex() || null == getScenarioIndex()) {
			return false;
		}
		return true;
	}
}
