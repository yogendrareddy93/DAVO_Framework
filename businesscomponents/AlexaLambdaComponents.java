package businesscomponents;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import supportlibraries.ReusableLibrary;
import supportlibraries.ScriptHelper;
import uimap.AmazonLoginPage;
import uimap.AmazonSkillPage;

import com.cognizant.framework.FrameworkException;
import com.cognizant.framework.Status;

/**
 * Class for storing general purpose business components
 * 
 * @author Cognizant
 */
public class AlexaLambdaComponents extends ReusableLibrary {

	String userName = dataTable.getData("General_Data", "Username");
	String password = dataTable.getData("General_Data", "Password");

	/**
	 * Constructor to initialize the component library
	 * 
	 * @param scriptHelper
	 *            The {@link ScriptHelper} object passed from the
	 *            {@link DriverScript}
	 */
	public AlexaLambdaComponents(ScriptHelper scriptHelper) {
		super(scriptHelper);
	}

	public void launchAmazonPortal() {
		try {

			driver.get(properties.getProperty("AmazonApplicationUrl"));
			report.updateTestLog("Invoke Application",
					"Application Launched succesfully", Status.DONE);
		} catch (Exception ex) {
			frameworkParameters.setStopExecution(true);
			throw new FrameworkException("Amazon Dev Portal",
					"Amazon Dev Portal Failed to Launch, hence stopping the execution"
							+ ex.getMessage());
		}
	}

	public void signInForAmazon() {
		try {

			driverUtil.waitUntilElementLocated(
					By.xpath(AmazonLoginPage.btnMainSignIn), 10);
			driver.findElement(By.xpath(AmazonLoginPage.btnMainSignIn)).click();

			driver.findElement(By.xpath(AmazonLoginPage.txtEmail)).sendKeys(
					userName);
			driver.findElement(By.xpath(AmazonLoginPage.txtPassword)).sendKeys(
					password);

			driver.findElement(By.xpath(AmazonLoginPage.btnSignIn)).click();
			report.updateTestLog("Sign In", "Signed In  succesfully",
					Status.PASS);
		} catch (Exception ex) {
			frameworkParameters.setStopExecution(true);
			throw new FrameworkException("Amazon Dev Portal",
					"Amazon Dev Portal Failed to Launch, hence stopping the execution"
							+ ex.getMessage());
		}
	}

	public void navigateToAlexaSkillInAmazon() {
		try {
			driverUtil.waitUntilElementEnabled(
					(By.xpath(AmazonSkillPage.homeObject)), 10);
			driver.findElement(By.xpath(AmazonSkillPage.homeObject)).click();
			driverUtil.waitUntilElementEnabled(
					(By.xpath(AmazonSkillPage.mainObject)), 10);
			driver.findElement(By.xpath(AmazonSkillPage.mainObject)).click();

			// Thread.sleep(8000);
			driverUtil.waitUntilElementLocated(
					By.xpath(AmazonSkillPage.skills), 10);
			List<WebElement> skillName = driver.getWebDriver().findElements(
					By.xpath(AmazonSkillPage.skills));

			Iterator<WebElement> iter = skillName.iterator();
			while (iter.hasNext()) {
				WebElement we = iter.next();

				if ((we.getText().equals(dataTable.getData("General_Data",
						"Skillname")))) {
					we.click();
					break;
				}
			}

			Thread.sleep(8000);
			driverUtil.waitUntilElementEnabled(
					By.xpath(AmazonSkillPage.appRoot), 10);
			driver.findElement(By.xpath(AmazonSkillPage.appRoot)).click();
			Thread.sleep(5000);
			report.updateTestLog("Alexa Skill Cliked",
					"Navigated to Alexa Skill", Status.PASS);
		} catch (Exception ex) {
			frameworkParameters.setStopExecution(true);
			throw new FrameworkException("Amazon Dev Portal",
					"Amazon Dev Portal Failed to Launch, hence stopping the execution"
							+ ex.getMessage());
		}
	}

	public void rateUtteranceForAmazon() throws InterruptedException {
		String utterance = dataTable.getData("Voice_Assistance", "Utterences");

		String expectedResponse = dataTable.getData("Voice_Assistance",
				"Expected");

		driverUtil.waitUntilElementEnabled(
				By.xpath(AmazonSkillPage.txtUtterances), 20);
		WebElement utteranceField = driver.findElement(By
				.xpath(AmazonSkillPage.txtUtterances));
		utteranceField.clear();
		utteranceField.click();
		utteranceField.sendKeys(utterance);

		Thread.sleep(2000);
		driver.findElement(By.xpath(AmazonSkillPage.btnAskAlexa)).click();
		Thread.sleep(4000);

		driverUtil.waitUntilElementEnabled(
				By.xpath(AmazonSkillPage.txtAlexaresponse), 80);

		WebElement response = driver.findElement(By
				.xpath(AmazonSkillPage.txtAlexaresponse));
		String actualAlexaResponse = response.getText()
				.replaceAll("\\<.*?>", "").replaceAll("^\"|\"$", "");

		validateAlexaresponse(utterance, expectedResponse, actualAlexaResponse);
	}

	private void validateAlexaresponse(String utterance,
			String expectedResponse, String actualAlexaResponse) {

		System.out.println(actualAlexaResponse);

		if (expectedResponse.equalsIgnoreCase(actualAlexaResponse)) {
			dataTable
					.putData("Voice_Assistance", "Actual", actualAlexaResponse);
			dataTable.putData("Voice_Assistance", "Result", "PASS");
		} else if ("".equals(actualAlexaResponse)) {

		} else {
			dataTable
					.putData("Voice_Assistance", "Actual", actualAlexaResponse);
			dataTable.putData("Voice_Assistance", "Result", "FAIL");
		}

		String actual_result = dataTable.getData("Voice_Assistance", "Actual");

		updateReport(utterance, expectedResponse, actual_result);
	}
	
	public void clickLogoutAmazon(){
		
		WebElement response = driver.findElement(By
				.xpath(AmazonLoginPage.btnLogout));
		response.click();
		report.updateTestLog("Logout", "Logout  succesfully", Status.PASS);
	}

}