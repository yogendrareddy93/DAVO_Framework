package allocator;

import org.openqa.selenium.WebDriver;

import supportlibraries.AlexaDriverScript;
import supportlibraries.AutomationDriverScript;
import supportlibraries.DriverScriptAbstract;

import com.cognizant.framework.FrameworkParameters;
import com.cognizant.framework.selenium.ExecutionMode;
import com.cognizant.framework.selenium.ResultSummaryManager;
import com.cognizant.framework.selenium.SeleniumTestParameters;
import com.cognizant.framework.selenium.WebDriverFactory;

/**
 * Class to facilitate parallel execution of test scripts
 * 
 * @author Cognizant
 */
public class ParallelRunner implements Runnable {
	private final SeleniumTestParameters testParameters;
	private int testBatchStatus = 0;

	public static WebDriver driver;

	/**
	 * Constructor to initialize the details of the test case to be executed
	 * 
	 * @param testParameters
	 *            The {@link SeleniumTestParameters} object (passed from the
	 *            {@link Allocator})
	 */
	ParallelRunner(SeleniumTestParameters testParameters) {
		super();

		this.testParameters = testParameters;
	}

	/**
	 * Function to get the overall test batch status
	 * 
	 * @return The test batch status (0 = Success, 1 = Failure)
	 */
	public int getTestBatchStatus() {
		return testBatchStatus;
	}

	@Override
	public void run() {
		FrameworkParameters frameworkParameters = FrameworkParameters
				.getInstance();
		String testReportName, executionTime, testStatus;

		if (frameworkParameters.getStopExecution()) {
			testReportName = "N/A";
			executionTime = "N/A";
			testStatus = "Aborted";
			testBatchStatus = 1; // Non-zero outcome indicates failure
		} else {

			//DriverScript driverScript = invokeDriverScript(this.testParameters);
			
			DriverScriptAbstract driverScript = invokeDriverScript(this.testParameters);

			testReportName = driverScript.getReportName();
			executionTime = driverScript.getExecutionTime();
			testStatus = driverScript.getTestStatus();

			if ("failed".equalsIgnoreCase(testStatus)) {
				testBatchStatus = 1; // Non-zero outcome indicates failure
			}
		}

		ResultSummaryManager resultSummaryManager = ResultSummaryManager
				.getInstance();
		resultSummaryManager.updateResultSummary(testParameters,
				testReportName, executionTime, testStatus);
	}

	/*private DriverScript invokeDriverScript(
			SeleniumTestParameters testParameters) {
		DriverScript driverScript;
		if (testParameters.equals(ExecutionMode.ALEXA)) {
			driverScript = new DriverScript(this.testParameters);
			if (driver == null) {
				launchWebDriverSession(this.testParameters);
			}
			driverScript.driveTestExecution(driver);
		} else {
			driverScript = new DriverScript(this.testParameters);
			driverScript.driveTestExecution();
		}

		return driverScript;

	}*/
	
	
	private DriverScriptAbstract invokeDriverScript(
			SeleniumTestParameters testParameters) {
		DriverScriptAbstract driverScript;
		if (testParameters.getExecutionMode().equals(ExecutionMode.ALEXA)) {
			driverScript = new AlexaDriverScript(this.testParameters);
			if (driver == null) {
				launchWebDriverSession(this.testParameters);
			}
			driverScript.driveTestExecution(driver);
		} else {
			driverScript = new AutomationDriverScript(this.testParameters);
			driverScript.driveTestExecution();
		}

		return driverScript;

	}

	private WebDriver launchWebDriverSession(
			SeleniumTestParameters testParameters) {
		driver = WebDriverFactory.getWebDriver(testParameters.getBrowser());
		return driver;
	}
}