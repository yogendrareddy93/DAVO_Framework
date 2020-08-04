package businesscomponents;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.cognizant.framework.Status;

import supportlibraries.ReusableLibrary;
import supportlibraries.ScriptHelper;
import uimap.ChatbotPage;


/**
 * Class for storing general purpose business components
 * 
 * @author Cognizant
 */
public class ChatbotComponents extends ReusableLibrary {

	String utterances = dataTable.getData("Voice_Assistance", "Utterences");
	String expectedResponse = dataTable.getData("Voice_Assistance", "Expected");
	String actualAlexaResponse;
	/**
	 * Constructor to initialize the component library
	 * 
	 * @param scriptHelper
	 *            The {@link ScriptHelper} object passed from the
	 *            {@link DriverScript}
	 */
	public ChatbotComponents(ScriptHelper scriptHelper) {
		super(scriptHelper);
	}

	
	public void launchChatbotPortal() throws IOException, InterruptedException {	
		
			driver.get(properties.getProperty("ChatUIUrl"));		
			report.updateTestLog("Launch ChatbotPortal", "Application Launched succesfully", Status.PASS);

	}
	
	public void invokeUtterances()
	{
			driver.findElement(By.id(ChatbotPage.txtUtterances)).sendKeys(utterances);
			driver.findElement(By.className(ChatbotPage.sendUtterances)).click();
			List<WebElement> liElements = driver.findElements(By.tagName(ChatbotPage.list));
			for (int i = 0; i < liElements.size();)
			{
	            int size=liElements.size(); 
	            int newsize=++size;
				WebElement linkElement = driver.findElement(By.xpath("//*[@id='chatWindow']/li[" + newsize + "]/div[2]/p"));
				actualAlexaResponse=linkElement.getText(); 
	          break;
			}
			validateAlexaresponse(utterances, expectedResponse, actualAlexaResponse);

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
	
	

}