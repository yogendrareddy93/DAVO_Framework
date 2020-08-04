package supportlibraries;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;

import com.cognizant.framework.FrameworkException;
import com.cognizant.framework.OnError;
import com.cognizant.framework.ReportSettings;
import com.cognizant.framework.ReportTheme;
import com.cognizant.framework.ReportThemeFactory;
import com.cognizant.framework.ReportThemeFactory.Theme;
import com.cognizant.framework.Status;
import com.cognizant.framework.TimeStamp;
import com.cognizant.framework.Util;
import com.cognizant.framework.selenium.Browser;
import com.cognizant.framework.selenium.CraftDriver;
import com.cognizant.framework.selenium.ExecutionMode;
import com.cognizant.framework.selenium.SeleniumReport;
import com.cognizant.framework.selenium.SeleniumTestParameters;
import com.cognizant.framework.selenium.WebDriverUtil;

/**
 * Driver script class which encapsulates the core logic of the framework
 * 
 * @author Cognizant
 */
public class AlexaDriverScript extends DriverScriptAbstract {

	/**
	 * DriverScript constructor
	 * 
	 * @param testParameters
	 *            A {@link SeleniumTestParameters} object
	 */
	public AlexaDriverScript(SeleniumTestParameters testParameters) {
		super(testParameters);
	}

	@Override
	public void driveTestExecution(WebDriver driver) {
		startUp();
		initializeTestIterations();
		initializeWebDriverAlexa(driver);
		initializeTestReport();
		initializeDatatable();
		executeCraftOrCraftLite();
		wrapUp();
	}

	private void executeCraftOrCraftLite() {
		if (properties.getProperty("Approach")
				.equalsIgnoreCase("KeywordDriven")) {
			initializeTestScript();
			executeCRAFTTestIterations();
		} else {
			initializeTestCase();

			try {
				testCase.setUp();
				executeCRAFTLiteTestIterations();
			} catch (FrameworkException fx) {
				exceptionHandler(fx, fx.getErrorName());
			} catch (Exception ex) {
				exceptionHandler(ex, "Error");
			} finally {
				testCase.tearDown(); // tearDown will ALWAYS be called
			}
		}

	}

	private void initializeWebDriverAlexa(WebDriver webDriver) {
		switch (testParameters.getExecutionMode()) {
		case ALEXA:
			driver = new CraftDriver(webDriver);
			driver.setTestParameters(testParameters);
			WaitPageLoad();
			break;
		default:
			throw new FrameworkException("Unhandled Execution Mode!");
		}
		implicitWaitForDriver();

	}

	private void implicitWaitForDriver() {
		long objectSyncTimeout = Long.parseLong(properties.get(
				"ObjectSyncTimeout").toString());
		driver.manage().timeouts()
				.implicitlyWait(objectSyncTimeout, TimeUnit.SECONDS);
	}

	private void WaitPageLoad() {
		long pageLoadTimeout = Long.parseLong(properties.get("PageLoadTimeout")
				.toString());
		driver.manage().timeouts()
				.pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
		driver.manage().window().maximize();

	}

	private void initializeTestReport() {
		initializeReportSettings();
		ReportTheme reportTheme = ReportThemeFactory.getReportsTheme(Theme
				.valueOf(properties.getProperty("ReportsTheme")));

		report = new SeleniumReport(reportSettings, reportTheme, testParameters);

		report.initialize();
		report.setDriver(driver);
		if (testParameters.getExecutionMode().equals(ExecutionMode.SEETEST)) {
			report.setClient(client);
		}
		report.initializeTestLog();
		createTestLogHeader();
	}

	private void initializeReportSettings() {
		if (System.getProperty("ReportPath") != null) {
			reportPath = System.getProperty("ReportPath");
		} else {
			reportPath = TimeStamp.getInstance();
		}

		reportSettings = new ReportSettings(reportPath,
				testParameters.getCurrentScenario() + "_"
						+ testParameters.getCurrentTestcase() + "_"
						+ testParameters.getCurrentTestInstance());

		reportSettings.setDateFormatString(properties
				.getProperty("DateFormatString"));
		reportSettings.setLogLevel(Integer.parseInt(properties
				.getProperty("LogLevel")));
		reportSettings.setProjectName(properties.getProperty("ProjectName"));
		reportSettings.setGenerateExcelReports(Boolean.parseBoolean(properties
				.getProperty("ExcelReport")));
		reportSettings.setGenerateHtmlReports(Boolean.parseBoolean(properties
				.getProperty("HtmlReport")));
		reportSettings.setGenerateSeeTestReports(Boolean
				.parseBoolean(mobileProperties
						.getProperty("SeeTestReportGeneration")));
		reportSettings.setGeneratePerfectoReports(Boolean
				.parseBoolean(mobileProperties
						.getProperty("PerfectoReportGeneration")));
		reportSettings.setTakeScreenshotFailedStep(Boolean
				.parseBoolean(properties
						.getProperty("TakeScreenshotFailedStep")));
		reportSettings.setTakeScreenshotPassedStep(Boolean
				.parseBoolean(properties
						.getProperty("TakeScreenshotPassedStep")));
		reportSettings.setConsolidateScreenshotsInWordDoc(Boolean
				.parseBoolean(properties
						.getProperty("ConsolidateScreenshotsInWordDoc")));
		reportSettings.setIsAlexaTestCase(isAlexaTestCase());
		if (testParameters.getBrowser().equals(Browser.HTML_UNIT)) {
			// Screenshots not supported in headless mode
			reportSettings.setLinkScreenshotsToTestLog(false);
		} else {
			reportSettings
					.setLinkScreenshotsToTestLog(this.linkScreenshotsToTestLog);
		}
	}

	private void createTestLogHeader() {
		report.addTestLogHeading(reportSettings.getProjectName() + " - "
				+ reportSettings.getReportName()
				+ " Automation Execution Results");
		report.addTestLogSubHeading(
				"Date & Time",
				": "
						+ Util.getFormattedTime(startTime,
								properties.getProperty("DateFormatString")),
				"Iteration Mode", ": " + testParameters.getIterationMode());
		report.addTestLogSubHeading("Start Iteration",
				": " + testParameters.getStartIteration(), "End Iteration",
				": " + testParameters.getEndIteration());

		switch (testParameters.getExecutionMode()) {
		case ALEXA:
			report.addTestLogSubHeading("ALEXA Testing Approach", ": "
					+ testParameters.getAlexaTestApproach(), "Execution on",
					": " + "Local Machine");
			break;

		default:
			throw new FrameworkException("Unhandled Execution Mode!");
		}

		report.addTestLogTableHeadings();
	}

	private void executeCRAFTTestIterations() {

		while (currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Conversation: "
					+ Integer.toString(currentIteration));

			// Evaluate each test iteration for any errors
			try {
				executeTestcase(businessFlowData);
			} catch (FrameworkException fx) {
				exceptionHandler(fx, fx.getErrorName());
			} catch (InvocationTargetException ix) {
				exceptionHandler((Exception) ix.getCause(), "Error");
			} catch (Exception ex) {
				exceptionHandler(ex, "Error");
			}

			currentIteration++;
		}
	}

	private void executeCRAFTLiteTestIterations() {
		while (currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Conversation: "
					+ Integer.toString(currentIteration));

			// Evaluate each test iteration for any errors
			try {
				testCase.executeTest();
			} catch (FrameworkException fx) {
				exceptionHandler(fx, fx.getErrorName());
			} catch (Exception ex) {
				exceptionHandler(ex, "Error");
			}

			currentIteration++;
			dataTable.setCurrentRow(testParameters.getCurrentTestcase(),
					currentIteration, 0);
		}
	}

	private void initializeTestCase() {
		driverUtil = new WebDriverUtil(driver);
		scriptHelper = new ScriptHelper(dataTable, report, driver, driverUtil,
				testParameters, dataBean);
		driver.setRport(report);
		testCase = getTestCaseInstance();
		testCase.initialize(scriptHelper);
	}

	private CRAFTLiteTestCase getTestCaseInstance() {
		Class<?> testScriptClass;
		try {
			testScriptClass = Class.forName("testscripts."
					+ testParameters.getCurrentScenario() + "."
					+ testParameters.getCurrentTestcase());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new FrameworkException(
					"The specified test case is not found!");
		}

		try {
			return (CRAFTLiteTestCase) testScriptClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(
					"Error while instantiating the specified test script");
		}
	}

	private void executeTestcase(List<String> businessFlowData)
			throws IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException {
		Map<String, Integer> keywordDirectory = new HashMap<String, Integer>();

		for (int currentKeywordNum = 0; currentKeywordNum < businessFlowData
				.size(); currentKeywordNum++) {
			String[] currentFlowData = businessFlowData.get(currentKeywordNum)
					.split(",");
			String currentKeyword = currentFlowData[0];

			int nKeywordIterations;
			if (currentFlowData.length > 1) {
				nKeywordIterations = Integer.parseInt(currentFlowData[1]);
			} else {
				nKeywordIterations = 1;
			}

			for (int currentKeywordIteration = 0; currentKeywordIteration < nKeywordIterations; currentKeywordIteration++) {
				if (keywordDirectory.containsKey(currentKeyword)) {
					keywordDirectory.put(currentKeyword,
							keywordDirectory.get(currentKeyword) + 1);
				} else {
					keywordDirectory.put(currentKeyword, 1);
				}
				currentSubIteration = keywordDirectory.get(currentKeyword);

				testParameters.setCurrentSubIteration(currentSubIteration);

				dataTable.setCurrentRow(testParameters.getCurrentTestcase(),
						currentIteration, currentSubIteration);

				if (currentSubIteration > 1) {
//					report.addTestLogSubSection(currentKeyword
//							+ " (Sub-Iteration: " + currentSubIteration + ")");
				} else {
					report.addTestLogSubSection(currentKeyword);
				}

				invokeBusinessComponent(currentKeyword);
				if (!(Util.currentComponentStatus == null)) {
					if (Util.currentComponentStatus.equalsIgnoreCase("Fail")) {
						break;
					}
				}
			}
		}
	}

	private void invokeBusinessComponent(String currentKeyword)
			throws IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException {
		Boolean isMethodFound = false;
		final String CLASS_FILE_EXTENSION = ".class";
		File[] packageDirectories = {
				new File(frameworkParameters.getRelativePath()
						+ Util.getFileSeparator() + "businesscomponents"),
				new File(frameworkParameters.getRelativePath()
						+ Util.getFileSeparator() + "componentgroups") };

		for (File packageDirectory : packageDirectories) {
			File[] packageFiles = packageDirectory.listFiles();
			String packageName = packageDirectory.getName();

			for (int i = 0; i < packageFiles.length; i++) {
				File packageFile = packageFiles[i];
				String fileName = packageFile.getName();

				// We only want the .class files
				if (fileName.endsWith(CLASS_FILE_EXTENSION)) {
					// Remove the .class extension to get the class name
					String className = fileName.substring(0, fileName.length()
							- CLASS_FILE_EXTENSION.length());

					Class<?> reusableComponents = Class.forName(packageName
							+ "." + className);
					Method executeComponent;

					try {
						// Convert the first letter of the method to lowercase
						// (in line with java naming conventions)
						currentKeyword = currentKeyword.substring(0, 1)
								.toLowerCase() + currentKeyword.substring(1);
						executeComponent = reusableComponents.getMethod(
								currentKeyword, (Class<?>[]) null);
					} catch (NoSuchMethodException ex) {
						// If the method is not found in this class, search the
						// next class
						continue;
					}

					isMethodFound = true;

					Constructor<?> ctor = reusableComponents
							.getDeclaredConstructors()[0];
					Object businessComponent = ctor.newInstance(scriptHelper);

					executeComponent.invoke(businessComponent, (Object[]) null);

					break;
				}
			}
		}

		if (!isMethodFound) {
			throw new FrameworkException("Keyword " + currentKeyword
					+ " not found within any class "
					+ "inside the businesscomponents package");
		}
	}

	private void exceptionHandler(Exception ex, String exceptionName) {
		// Error reporting
		String exceptionDescription = ex.getMessage();
		if (exceptionDescription == null) {
			exceptionDescription = ex.toString();
		}

		if (ex.getCause() != null) {
			if (isAlexaTestCase()) {
				report.updateTestLog(exceptionName, "NA", exceptionDescription
						+ " <b>Caused by: </b>" + ex.getCause(), Status.FAIL);
			} else {
				report.updateTestLog(exceptionName, exceptionDescription
						+ " <b>Caused by: </b>" + ex.getCause(), Status.FAIL);
			}

		} else {

			if (isAlexaTestCase()) {
				report.updateTestLog(exceptionName, "NA", exceptionDescription,
						Status.FAIL);
			} else {
				report.updateTestLog(exceptionName, exceptionDescription,
						Status.FAIL);
			}

		}

		// Print stack trace for detailed debug information
		StringWriter stringWriter = new StringWriter();
		ex.printStackTrace(new PrintWriter(stringWriter));
		String stackTrace = stringWriter.toString();
		report.updateTestLog("Exception stack trace", stackTrace, Status.DEBUG);

		// Error response
		if (frameworkParameters.getStopExecution()) {
			report.updateTestLog(
					"CRAFT Info",
					"Test execution terminated by user! All subsequent tests aborted...",
					Status.DONE);
			currentIteration = testParameters.getEndIteration();
		} else {
			OnError onError = OnError
					.valueOf(properties.getProperty("OnError"));
			switch (onError) {
			// Stop option is not relevant when run from QC
			case NEXT_ITERATION:

				if (isAlexaTestCase()) {

					report.updateTestLog(
							"CRAFT Info",
							"Test case iteration terminated by user! Proceeding to next iteration (if applicable)...",
							"NA", Status.DONE);

				} else {
					report.updateTestLog(
							"CRAFT Info",
							"Test case iteration terminated by user! Proceeding to next iteration (if applicable)...",
							Status.DONE);
				}

				break;

			case NEXT_TESTCASE:

				if (isAlexaTestCase()) {

					report.updateTestLog(
							"CRAFT Info",
							"Test case iteration terminated by user! Proceeding to next iteration (if applicable)...",
							"NA", Status.DONE);

				} else {
					report.updateTestLog(
							"CRAFT Info",
							"Test case terminated by user! Proceeding to next test case (if applicable)...",
							"NA", Status.DONE);
				}

				currentIteration = testParameters.getEndIteration();
				break;

			case STOP:
				frameworkParameters.setStopExecution(true);

				if (isAlexaTestCase()) {

					report.updateTestLog(
							"CRAFT Info",
							"Test case iteration terminated by user! Proceeding to next iteration (if applicable)...",
							"NA", Status.DONE);

				} else {
					report.updateTestLog(
							"CRAFT Info",
							"Test execution terminated by user! All subsequent tests aborted...",
							"NA", Status.DONE);
				}

				currentIteration = testParameters.getEndIteration();
				break;

			default:
				throw new FrameworkException("Unhandled OnError option!");
			}
		}
	}

	private boolean isAlexaTestCase() {
		boolean isAlexaTestCase = false;
		if (testParameters.getExecutionMode().equals(ExecutionMode.ALEXA)
				&& (testParameters.getCurrentTestcase().contains("Utterances")
						)) {
			isAlexaTestCase = true;
		}
		return isAlexaTestCase;
	}

	@Override
	public void driveTestExecution() {
		// TODO Auto-generated method stub

	}

}