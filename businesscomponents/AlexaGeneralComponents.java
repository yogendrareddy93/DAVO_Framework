package businesscomponents;

import java.io.IOException;

import supportlibraries.ReusableLibrary;
import supportlibraries.ScriptHelper;

/**
 * Class for storing general purpose business components
 * 
 * @author Cognizant
 */
public class AlexaGeneralComponents extends ReusableLibrary {

	AlexaLambdaComponents alexaApiComponents = new AlexaLambdaComponents(scriptHelper);
	AlexaVoiceComponents alexaVoiceComponents = new AlexaVoiceComponents(
			scriptHelper);

	/**
	 * Constructor to initialize the component library
	 * 
	 * @param scriptHelper
	 *            The {@link ScriptHelper} object passed from the
	 *            {@link DriverScript}
	 */
	public AlexaGeneralComponents(ScriptHelper scriptHelper) {
		super(scriptHelper);
	}

	public void invokeApplication() {

		if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"LAMBDA_APPROACH")) {

			alexaApiComponents.launchAmazonPortal();
		} else if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"VOICE_APPROACH")) {

			alexaVoiceComponents.launchAlexPortal();
		}

	}

	public void signIn() throws InterruptedException {

		if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"LAMBDA_APPROACH")) {

			alexaApiComponents.signInForAmazon();
		} else if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"VOICE_APPROACH")) {

			alexaVoiceComponents.signInForAlexa();
		}

	}

	public void navigateToAlexaSkill() throws InterruptedException {

		if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"LAMBDA_APPROACH")) {

			alexaApiComponents.navigateToAlexaSkillInAmazon();
		} else if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"VOICE_APPROACH")) {

			alexaVoiceComponents.navigateToHistoryInAlexa();
		}
	}

	public void rateUtterances() throws InterruptedException, IOException {

		if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"LAMBDA_APPROACH")) {

			alexaApiComponents.rateUtteranceForAmazon();
		} else if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"VOICE_APPROACH")) {

			alexaVoiceComponents.rateUtteranceWithVoice();
		}

	}

	public void clickLogout() throws InterruptedException {

		if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"LAMBDA_APPROACH")) {

			alexaApiComponents.clickLogoutAmazon();
		} else if (testParameters.getAlexaTestApproach().equalsIgnoreCase(
				"VOICE_APPROACH")) {

			alexaVoiceComponents.clickLogoutAlexa();
		}

	}

}