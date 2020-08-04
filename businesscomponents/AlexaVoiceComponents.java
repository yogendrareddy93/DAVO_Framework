package businesscomponents;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import supportlibraries.ReusableLibrary;
import supportlibraries.ScriptHelper;
import uimap.AlexaHomePage;
import uimap.AlexaLoginPage;

import com.cognizant.framework.FrameworkException;
import com.cognizant.framework.Status;

/**
 * Class for storing general purpose business components
 * 
 * @author Cognizant
 */
public class AlexaVoiceComponents extends ReusableLibrary {

	String userName = dataTable.getData("General_Data", "Username");
	String password = dataTable.getData("General_Data", "Password");

	/**
	 * Constructor to initialize the component library
	 * 
	 * @param scriptHelper
	 *            The {@link ScriptHelper} object passed from the
	 *            {@link DriverScript}
	 */
	public AlexaVoiceComponents(ScriptHelper scriptHelper) {
		super(scriptHelper);
	}

	public void launchAlexPortal() {
		try {
			driver.get(properties.getProperty("AlexaApplicationUrl"));
			report.updateTestLog("Invoke Application", "Application Launched succesfully", Status.DONE);
		} catch (Exception ex) {
			frameworkParameters.setStopExecution(true);
			throw new FrameworkException("Alexa Dev Portal",
					"Alexa Dev Portal Failed to Launch, hence stopping the execution" + ex.getMessage());
		}
	}

	public void signInForAlexa() {
		try {

			driverUtil.waitUntilElementLocated(By.id(AlexaLoginPage.btnSignIn), 10);
			driver.findElement(By.id(AlexaLoginPage.txtEmail)).sendKeys(userName);
			driver.findElement(By.id(AlexaLoginPage.txtPassword)).sendKeys(password);
			driver.findElement(By.id(AlexaLoginPage.btnSignIn)).click();

		} catch (Exception ex) {
			frameworkParameters.setStopExecution(true);
			throw new FrameworkException("Alexa Dev Portal",
					"Alexa Dev Portal Failed to Launch, hence stopping the execution" + ex.getMessage());
		}
	}

	public void navigateToHistoryInAlexa() {
		try {
			try {
				// driverUtil.waitUntilElementEnabled(
				// By.id(AlexaHomePage.btniSettings), 60);
				// driverUtil.fluentWait(AlexaHomePage.btniSettings);
				Thread.sleep(20000);
			} catch (Exception ex) {
				if (driverUtil.objectExists(By.id(AlexaLoginPage.btnSignIn))) {
					signInForAlexa();
					Thread.sleep(40000);
					// driverUtil.waitUntilElementEnabled(
					// By.id(AlexaHomePage.btniSettings), 60);
					// driverUtil.fluentWait(AlexaHomePage.btniSettings);
				}
			}

			driver.findElement(By.id(AlexaHomePage.btniSettings)).click();

			driver.findElement(By.xpath(AlexaHomePage.btnHistory)).click();

		} catch (Exception ex) {
			frameworkParameters.setStopExecution(true);
			throw new FrameworkException("Alexa Dev Portal",
					"Alexa Dev Portal Failed to Launch, hence stopping the execution" + ex.getMessage());
		}
	}

	public void rateUtteranceWithVoice() throws IOException, InterruptedException {

		String utterance = dataTable.getData("Voice_Assistance", "Utterences");

		String expectedResponse = dataTable.getData("Voice_Assistance", "Expected");

		invokeVoiceUtterance(utterance);

		getHistoryNValidate(utterance, expectedResponse);
	}

	private void getHistoryNValidate(String utterance, String expectedResponse) throws InterruptedException {

		int i = 0;
		int responseCode = 0;
		String concatenatedUtterance="alexa".concat(dataTable.getData("Voice_Assistance", "InvocationName")).concat(dataTable.getData("Voice_Assistance", "Skill_Name")).concat(utterance);

		while (i < 2) {

			driver.navigate().refresh();
			driverUtil.fluentWaitUntilStaleElement(By.className(AlexaHomePage.historyInfoBar), 10);
			List<WebElement> gridElements = driver.findElements(By.className(AlexaHomePage.gridHistory));

			String historyUtterance = gridElements.get(0).getText().split("\n")[0];
			String secondHistoryUtterance = gridElements.get(1).getText().split("\n")[0];
			dataBean.setCurrentUtterance(historyUtterance);

			if (dataBean.getCurrentUtterance().equals(dataBean.getLastUtterance())) {

				if (dataBean.getCurrentUtterance().equals(secondHistoryUtterance)) {
					verifyUtterance(utterance, expectedResponse, gridElements);
					break;
				}

			} else if (StringUtils.getJaroWinklerDistance(dataBean.getCurrentUtterance(), concatenatedUtterance) > 0.65) {

				verifyUtterance(utterance, expectedResponse, gridElements);
				break;

			} else if (historyUtterance.equalsIgnoreCase("Text not available. Click to play recording.")) {

				gridElements.get(0).click();
				driver.findElement(By.xpath(AlexaHomePage.deleteAlexaResponse)).click();
				driverUtil.fluentWaitUntilStaleElement(By.className(AlexaHomePage.historyInfoBar), 10);
				dataBean.setCurrentUtterance(
						driver.findElements(By.className(AlexaHomePage.gridHistory)).get(0).getText().split("\n")[0]);
				if (StringUtils.getJaroWinklerDistance(dataBean.getCurrentUtterance(), concatenatedUtterance) > 0.65) {
					verifyUtterance(utterance, expectedResponse, gridElements);
					break;
				}
				responseCode = 2;
				i++;
			} else if (historyUtterance.contains("Hidden")) {
				Thread.sleep(500);
				responseCode = 3;
				i++;
			} else if (historyUtterance.equalsIgnoreCase("Alexa")
					|| historyUtterance.equalsIgnoreCase(dataTable.getData("Voice_Assistance", "InvocationName"))
					|| historyUtterance.equalsIgnoreCase(dataTable.getData("Voice_Assistance", "Skill_Name"))) {

				gridElements.get(0).click();
				driver.findElement(By.xpath(AlexaHomePage.deleteAlexaResponse)).click();
				driverUtil.fluentWaitUntilStaleElement(By.className(AlexaHomePage.historyInfoBar), 5);
				dataBean.setCurrentUtterance(
						driver.findElements(By.className(AlexaHomePage.gridHistory)).get(0).getText().split("\n")[0]);
				if (StringUtils.getJaroWinklerDistance(dataBean.getCurrentUtterance(), concatenatedUtterance) > 0.65) {
					verifyUtterance(utterance, expectedResponse, gridElements);
					break;
				}
				responseCode = 4;
				i++;
			}

			else {
				Thread.sleep(500);
				responseCode = 5;
				i++;
			}

		}

		updateNagativeScenarioReport(i, utterance, expectedResponse, responseCode);

	}

	private void verifyUtterance(String utterance, String expectedResponse, List<WebElement> gridElements) {
		driverUtil.fluentWaitUntilStaleElement(By.className(AlexaHomePage.historyInfoBar), 10);

		gridElements.get(0).click();
		try {
			dataBean.setCurrentUtteranceResponse(
					driver.findElement(By.xpath(AlexaHomePage.UttAlexaResponse)).getText());
		} catch (Exception e) {
			dataBean.setCurrentUtteranceResponse(
					"Utterance received by Alexa but it didn't provide the Response, please retry this Utterance");
			driver.findElement(By.xpath(AlexaHomePage.deleteAlexaResponse)).click();
			// d-error-message handling
		}
		validateAlexaresponse(utterance, expectedResponse, dataBean.getCurrentUtteranceResponse());
		dataBean.setLastUtteranceResponse(dataBean.getCurrentUtteranceResponse());
		driver.findElement(By.xpath(AlexaHomePage.deleteAlexaResponse)).click();
	}

	private void updateNagativeScenarioReport(int i, String utterance, String expectedResponse, int responseCode) {

		if (i >= 2) {
			switch (responseCode) {
			case 2:
				report.updateTestLog(utterance, expectedResponse,
						"Alexa did not receive the Utterance or Utterance mismatched", Status.FAIL);
				break;
			case 3:
				report.updateTestLog(utterance, expectedResponse,
						"Permission denied to view the response. Please reach the developer to enable history view",
						Status.FAIL);
				break;
			case 4:
				report.updateTestLog(utterance, expectedResponse,
						"Alexa received Utterance but it doesnot match with Uttered Utterance", Status.FAIL);
				break;
			case 5:
				report.updateTestLog(utterance, expectedResponse,
						"Alexa Didn't receive the uttered Utterance and it didn't provide the response", Status.FAIL);
				break;
			default:
				break;

			}
		}

	}

	private void validateAlexaresponse(String utterance, String expectedResponse, String actualAlexaResponse) {

		System.out.println(actualAlexaResponse);

		if (expectedResponse.equalsIgnoreCase(actualAlexaResponse)) {
			dataTable.putData("Voice_Assistance", "Actual", actualAlexaResponse);
			dataTable.putData("Voice_Assistance", "Result", "PASS");
		} else if ("".equals(actualAlexaResponse)) {

		} else {
			dataTable.putData("Voice_Assistance", "Actual", actualAlexaResponse);
			dataTable.putData("Voice_Assistance", "Result", "FAIL");
		}

		String actual_result = dataTable.getData("Voice_Assistance", "Actual");

		updateReport(utterance, expectedResponse, actual_result);
	}

	private void invokeVoiceUtterance(String utterance) throws IOException, InterruptedException {
		String whatToRun;
		if (testParameters.getCurrentSubIteration() == 1) {
			whatToRun = "say -v " + dataTable.getData("Voice_Assistance", "Voice") + " Alexa! "
					+ dataTable.getData("Voice_Assistance", "InvocationName") + " "
					+ dataTable.getData("Voice_Assistance", "Skill_Name") + " "
					+ dataTable.getData("Voice_Assistance", "Utterences") + "";

		}
		
		
		else {
			whatToRun = "say -v " + dataTable.getData("Voice_Assistance", "Voice") + " Alexa! "
					+ dataTable.getData("Voice_Assistance", "Utterences") + "";
		}
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(whatToRun);
		int exitVal = proc.waitFor();
		System.out.println("Process exitValue:" + exitVal);

		int exitVal1 = Integer.parseInt(dataTable.getData("Voice_Assistance", "Timeunits"));
		try {
			TimeUnit.SECONDS.sleep(exitVal1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void clickLogoutAlexa() {
		WebElement response = driver.findElement(By.xpath(AlexaLoginPage.btnLogout));
		response.click();
		report.updateTestLog("Logout", "Logout  succesfully", Status.PASS);
	}

}