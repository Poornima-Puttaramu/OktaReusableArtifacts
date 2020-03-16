
package ldap.account.migration.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The Factor class is a Bean class
 * @author PWC-AC
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Factor {
	private String factorType;
	private String provider = "OKTA";
	private Profile profile;

	public String getFactorType() {
		return factorType;
	}

	public void setFactorType(String factorType) {
		this.factorType = factorType;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Factor() {

	}

	public Factor(String factorType, String phoneNumber) {
		this.factorType = factorType;
		this.profile = new Profile(phoneNumber);
	}

	public boolean validate() {
		return profile.validate() && isValidString(provider) && isValidFactor(factorType);
	}

	private boolean isValidFactor(String factorType) {
		return "sms".equals(factorType) || "call".equals(factorType);
	}

	private boolean isValidString(String s) {
		return s != null && !s.isEmpty();
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Profile {
		private String phoneNumber;

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public Profile() {

		}

		public Profile(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		boolean validate() {
			return isValidString(phoneNumber);
		}
	}
}
