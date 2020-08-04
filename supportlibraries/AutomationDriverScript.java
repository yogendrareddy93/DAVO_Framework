package supportlibraries;

import io.appium.java_client.AppiumDriver;

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
import com.cognizant.framework.selenium.AppiumDriverFactory;
import com.cognizant.framework.selenium.Browser;
import com.cognizant.framework.selenium.BrowserStackDriverFactory;
import com.cognizant.framework.selenium.CraftDriver;
import com.cognizant.framework.selenium.ExecutionMode;
import com.cognizant.framework.selenium.HPMobileCenterDriverFactory;
import com.cognizant.framework.selenium.MintDriverFactory;
import com.cognizant.framework.selenium.MobileLabsDriverFactory;
import com.cognizant.framework.selenium.MobileToolName;
import com.cognizant.framework.selenium.PerfectoDriverFactory;
import com.cognizant.framework.selenium.SauceLabsDriverFactory;
import com.cognizant.framework.selenium.SeeTestDriverFactory;
import com.cognizant.framework.selenium.SeleniumReport;
import com.cognizant.framework.selenium.SeleniumTestParameters;
import com.cognizant.framework.selenium.WebDriverFactory;
import com.cognizant.framework.selenium.WebDriverUtil;
import com.experitest.selenium.MobileWebDriver;

/**
 * Driver script class which encapsulates the core logic of the framework
 * 
 * @author Cognizant
 */
public class AutomationDriverScript extends DriverScriptAbstract {

	/**
	 * DriverScript constructor
	 * 
	 * @param testParameters
	 *            A {@link SeleniumTestParameters} object
	 */
	public AutomationDriverScript(SeleniumTestParameters testParameters) {
		super(testParameters);
	}

	public void driveTestExecution() {
		startUp();
		initializeTestIterations();
		initializeWebDriver();
		initializeTestReport();
		initializeDatatable();
		executeCraftOrCraftLite();
		quitWebDriver();
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

	@SuppressWarnings("rawtypes")
	private void initializeWebDriver() {

		switch (testParameters.getExecutionMode()) {

		case LOCAL:
			WebDriver webDriver = WebDriverFactory.getWebDriver(testParameters
					.getBrowser());
			driver = new CraftDriver(webDriver);
			driver.setTestParameters(testParameters);
			WaitPageLoad();
			break;

		case REMOTE:
			WebDriver remoteWebDriver = WebDriverFactory.getRemoteWebDriver(
					testParameters.getBrowser(),
					properties.getProperty("RemoteUrl"));
			driver = new CraftDriver(remoteWebDriver);
			driver.setTestParameters(testParameters);
			WaitPageLoad();
			break;

		case LOCAL_EMULATED_DEVICE:
			testParameters.setBrowser(Browser.CHROME); // Mobile emulation
														// supported only on
														// Chrome
			WebDriver localEmulatedDriver = WebDriverFactory
					.getEmulatedWebDriver(testParameters.getDeviceName());
			driver = new CraftDriver(localEmulatedDriver);
			driver.setTestParameters(testParameters);
			WaitPageLoad();
			break;

		case REMOTE_EMULATED_DEVICE:
			testParameters.setBrowser(Browser.CHROME); // Mobile emulation
														// supported only on
														// Chrome
			WebDriver remoteEmulatedDriver = WebDriverFactory
					.getEmulatedRemoteWebDriver(testParameters.getDeviceName(),
							properties.getProperty("RemoteUrl"));
			driver = new CraftDriver(remoteEmulatedDriver);
			driver.setTestParameters(testParameters);
			break;

		case GRID:
			WebDriver remoteGridDriver = WebDriverFactory.getRemoteWebDriver(
					testParameters.getBrowser(),
					testParameters.getBrowserVersion(),
					testParameters.getPlatform(),
					properties.getProperty("RemoteUrl"));
			driver = new CraftDriver(remoteGridDriver);
			driver.setTestParameters(testParameters);
			WaitPageLoad();
			break;

		case MOBILE:
			if ((testParameters.getMobileToolName()
					.equals(MobileToolName.DEFAULT))
					|| (testParameters.getMobileToolName()
							.equals(MobileToolName.APPIUM))) {
				WebDriver appiumDriver = AppiumDriverFactory.getAppiumDriver(
						testParameters.getMobileExecutionPlatform(),
						testParameters.getDeviceName(),
						testParameters.getMobileOSVersion(),
						testParameters.shouldInstallApplication(),
						mobileProperties.getProperty("AppiumURL"));
				driver = new CraftDriver(appiumDriver);
				driver.setTestParameters(testParameters);
			} else if (testParameters.getMobileToolName().equals(
					MobileToolName.REMOTE_WEBDRIVER)) {
				WebDriver remoteAppiumDriver = AppiumDriverFactory
						.getAppiumRemoteWebDriver(
								testParameters.getMobileExecutionPlatform(),
								testParameters.getDeviceName(),
								testParameters.getMobileOSVersion(),
								testParameters.shouldInstallApplication(),
								mobileProperties.getProperty("AppiumURL"));
				driver = new CraftDriver(remoteAppiumDriver);
				driver.setTestParameters(testParameters);
			}

			break;

		case PERFECTO:

			if (testParameters.getMobileToolName()
					.equals(MobileToolName.APPIUM)) {
				WebDriver appiumPerfectoDriver = PerfectoDriverFactory
						.getPerfectoAppiumDriver(
								testParameters.getMobileExecutionPlatform(),
								testParameters.getDeviceName(),
								mobileProperties.getProperty("PerfectoHost"));
				driver = new CraftDriver(appiumPerfectoDriver);
				driver.setTestParameters(testParameters);

			} else if (testParameters.getMobileToolName().equals(
					MobileToolName.REMOTE_WEBDRIVER)) {
				WebDriver remotePerfectoDriver = PerfectoDriverFactory
						.getPerfectoRemoteWebDriver(
								testParameters.getMobileExecutionPlatform(),
								testParameters.getDeviceName(),
								mobileProperties.getProperty("PerfectoHost"),
								testParameters.getBrowser());
				driver = new CraftDriver(remotePerfectoDriver);
				driver.setTestParameters(testParameters);
			}

			break;
		case SAUCELABS:

			if (testParameters.getMobileToolName()
					.equals(MobileToolName.APPIUM)) {
				AppiumDriver appiumSauceDriver = SauceLabsDriverFactory
						.getSauceAppiumDriver(
								testParameters.getMobileExecutionPlatform(),
								testParameters.getDeviceName(),
								mobileProperties.getProperty("SauceHost"),
								testParameters);
				driver = new CraftDriver(appiumSauceDriver);
				driver.setTestParameters(testParameters);

			} else if (testParameters.getMobileToolName().equals(
					MobileToolName.REMOTE_WEBDRIVER)) {
				WebDriver remoteSauceDriver = SauceLabsDriverFactory
						.getSauceRemoteWebDriver(
								mobileProperties.getProperty("SauceHost"),
								testParameters.getBrowser(),
								testParameters.getBrowserVersion(),
								testParameters.getPlatform(), testParameters);

				driver = new CraftDriver(remoteSauceDriver);
				driver.setTestParameters(testParameters);
			}

			break;
		case BROWSERSTACK:

			if (testParameters.getMobileToolName().equals(
					MobileToolName.REMOTE_WEBDRIVER)) {
				WebDriver browserstackRemoteDrivermobile = BrowserStackDriverFactory
						.getBrowserStackRemoteWebDriverMobile(testParameters
								.getMobileExecutionPlatform(), testParameters
								.getDeviceName(), mobileProperties
								.getProperty("BrowserStackHost"),
								testParameters);
				driver = new CraftDriver(browserstackRemoteDrivermobile);
				driver.setTestParameters(testParameters);

			} else if (testParameters.getMobileToolName().equals(
					MobileToolName.DEFAULT)) {
				WebDriver browserstackRemoteDriver = BrowserStackDriverFactory
						.getBrowserStackRemoteWebDriver(mobileProperties
								.getProperty("BrowserStackHost"),
								testParameters.getBrowser(), testParameters
										.getBrowserVersion(), testParameters
										.getPlatform(), testParameters);

				driver = new CraftDriver(browserstackRemoteDriver);
				driver.setTestParameters(testParameters);
			}

			break;
		case MINT:
			testParameters.setMobileToolName(MobileToolName.APPIUM);
			WebDriver mintAppiumtDriver = MintDriverFactory
					.getMintAppiumDriver(
							testParameters.getMobileExecutionPlatform(),
							testParameters.getDeviceName(),
							mobileProperties.getProperty("MintHost"),
							testParameters.getMobileOSVersion());
			driver = new CraftDriver(mintAppiumtDriver);
			driver.setTestParameters(testParameters);
			break;
		case SEETEST:

			testParameters.setMobileToolName(MobileToolName.DEFAULT);
			MobileWebDriver seeTestDriver = SeeTestDriverFactory
					.getSeeTestDriver(
							mobileProperties.getProperty("SeeTestHost",
									"localhost"),
							Integer.parseInt(testParameters.getSeeTestPort()),
							mobileProperties
									.getProperty("SeeTestProjectBaseDirectory"),
							mobileProperties.getProperty("SeeTestReportType",
									"xml"),
							"report",
							"Test Name from Driver Init",
							testParameters.getMobileExecutionPlatform(),
							mobileProperties
									.getProperty("SeeTestAndroidApplicationName"),
							mobileProperties
									.getProperty("SeeTestiOSApplicationName"),
							mobileProperties
									.getProperty("SeeTestAndroidWebApplicationName"),
							mobileProperties
									.getProperty("SeeTestiOSWebApplicationName"),
							testParameters.getDeviceName());
			driver = new CraftDriver(seeTestDriver);
			client = seeTestDriver.client;
			driver.setSeeTestDriver(seeTestDriver);
			driver.setTestParameters(testParameters);

			break;

		case MOBILELABS:

			testParameters.setMobileToolName(MobileToolName.REMOTE_WEBDRIVER);
			WebDriver mobilelabsDriver = MobileLabsDriverFactory
					.getMobileLabsDriver(
							testParameters.getMobileExecutionPlatform(),
							testParameters.getDeviceName(),
							mobileProperties.getProperty("AppiumURL"),
							testParameters.getMobileOSVersion());
			driver = new CraftDriver(mobilelabsDriver);
			driver.setTestParameters(testParameters);
			break;
		case MOBILECENTER:
			testParameters.getMobileToolName().equals(MobileToolName.APPIUM);
			WebDriver mobileCenterDriver = HPMobileCenterDriverFactory
					.getMobileCenterAppiumDriver(
							testParameters.getMobileExecutionPlatform(),
							testParameters.getDeviceName(),
							mobileProperties.getProperty("MobileCenterHost"));
			driver = new CraftDriver(mobileCenterDriver);
			driver.setTestParameters(testParameters);
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
		reportSettings.setisMobileExecution(isMobileAutomation());
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
		case LOCAL:
			report.addTestLogSubHeading("Browser/Platform", ": "
					+ testParameters.getBrowserAndPlatform(), "Execution on",
					": " + "Local Machine");
			break;

		case REMOTE:
			report.addTestLogSubHeading(
					"Browser/Platform",
					": " + testParameters.getBrowserAndPlatform(),
					"Executed on",
					": " + "Remote Machine @ "
							+ properties.getProperty("RemoteUrl"));
			break;

		case LOCAL_EMULATED_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": "
					+ testParameters.getBrowserAndPlatform(), "Executed on",
					": " + "Emulated Mobile Device on Local Machine");
			report.addTestLogSubHeading("Emulated Device Name", ": "
					+ testParameters.getDeviceName(), "", "");
			break;

		case REMOTE_EMULATED_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": "
					+ testParameters.getBrowserAndPlatform(), "Executed on",
					": " + "Emulated Mobile Device on Remote Machine @ "
							+ properties.getProperty("RemoteUrl"));
			report.addTestLogSubHeading("Emulated Device Name", ": "
					+ testParameters.getDeviceName(), "", "");
			break;

		case GRID:
			report.addTestLogSubHeading("Browser/Platform", ": "
					+ testParameters.getBrowserAndPlatform(), "Executed on",
					": " + "Grid @ " + properties.getProperty("RemoteUrl"));
			break;

		case MOBILE:
			report.addTestLogSubHeading("Execution Mode",
					": " + testParameters.getExecutionMode(),
					"Execution Platform",
					": " + testParameters.getMobileExecutionPlatform());
			report.addTestLogSubHeading("Tool Used",
					": " + testParameters.getMobileToolName(),
					"Device Name/ID", ": " + testParameters.getDeviceName());
			break;

		case PERFECTO:
			report.addTestLogSubHeading("Execution Mode",
					": " + testParameters.getExecutionMode(),
					"Execution Platform",
					": " + testParameters.getMobileExecutionPlatform());
			report.addTestLogSubHeading("Tool Used",
					": " + testParameters.getMobileToolName(),
					"Device Name/ID", ": " + testParameters.getDeviceName());
			report.addTestLogSubHeading(
					"Executed on",
					": " + "Perfecto MobileCloud @ "
							+ mobileProperties.getProperty("PerfectoHost"),
					"Perfecto User",
					": " + mobileProperties.getProperty("PerfectoUser"));
			break;
		case SAUCELABS:
			if (testParameters.getMobileToolName().toString()
					.equalsIgnoreCase("REMOTE_WEBDRIVER")) {
				report.addTestLogSubHeading("Execution Mode", ": "
						+ testParameters.getExecutionMode(),
						"Execution Platform",
						": " + testParameters.getPlatform());
				report.addTestLogSubHeading("Tool Used",
						": " + testParameters.getMobileToolName(), "Browser",
						": " + testParameters.getBrowser());
			} else {
				report.addTestLogSubHeading("Execution Mode", ": "
						+ testParameters.getExecutionMode(),
						"Execution Platform",
						": " + testParameters.getMobileExecutionPlatform());
				report.addTestLogSubHeading("Tool Used",
						": " + testParameters.getMobileToolName(),
						"Device Name/ID", ": " + testParameters.getDeviceName());
			}
			break;
		case SEETEST:
			report.addTestLogSubHeading("Execution Mode",
					": " + testParameters.getExecutionMode(),
					"Executed Platform",
					": " + testParameters.getMobileExecutionPlatform());
			report.addTestLogSubHeading("Tool Used",
					": " + testParameters.getMobileToolName(),
					"Device Name/ID", ": " + testParameters.getDeviceName());
			break;
		case MOBILELABS:
			report.addTestLogSubHeading("Execution Mode",
					": " + testParameters.getExecutionMode(),
					"Execution Platform",
					": " + testParameters.getMobileExecutionPlatform());
			report.addTestLogSubHeading("Tool Used",
					": " + testParameters.getMobileToolName(),
					"Device Name/ID", ": " + testParameters.getDeviceName());
			report.addTestLogSubHeading(
					"Executed on",
					": " + "MobileLabs Cloud @ "
							+ mobileProperties.getProperty("HostIP"),
					"MobileLabs User",
					": " + mobileProperties.getProperty("UserName"));
			break;
		case BROWSERSTACK:
			if (testParameters.getMobileToolName().toString()
					.equalsIgnoreCase("REMOTE_WEBDRIVER")) {
				report.addTestLogSubHeading("ExecutionPlatform", ": "
						+ testParameters.getExecutionMode(), "Executed on",
						": " + testParameters.getMobileExecutionPlatform());
				report.addTestLogSubHeading("Tool Used",
						": " + testParameters.getMobileToolName(),
						"Device Name/ID", ": " + testParameters.getDeviceName());
			} else {
				report.addTestLogSubHeading("ExecutionPlatform", ": "
						+ testParameters.getExecutionMode(), "Executed on",
						": " + testParameters.getPlatform());
				report.addTestLogSubHeading("Tool Used",
						": " + testParameters.getMobileToolName(), "Browser",
						": " + testParameters.getBrowser());
			}
			break;
		case MINT:
			report.addTestLogSubHeading("Execution Mode",
					": " + testParameters.getExecutionMode(),
					"Execution Platform",
					": " + testParameters.getMobileExecutionPlatform());
			report.addTestLogSubHeading("Tool Used",
					": " + testParameters.getMobileToolName(),
					"Device OS version",
					": " + testParameters.getMobileOSVersion());
			report.addTestLogSubHeading("Executed on", ": " + "Mint Cloud @ "
					+ mobileProperties.getProperty("MintHost"), "Mint User",
					": " + mobileProperties.getProperty("MintUsername"));
			break;

		case MOBILECENTER:
			report.addTestLogSubHeading("ExecutionPlatform", ": "
					+ testParameters.getExecutionMode(), "Executed on", ": "
					+ testParameters.getMobileExecutionPlatform());
			report.addTestLogSubHeading("Tool Used",
					": " + testParameters.getMobileToolName(),
					"Device Name/ID", ": " + testParameters.getDeviceName());
			break;

		default:
			throw new FrameworkException("Unhandled Execution Mode!");
		}

		report.addTestLogTableHeadings();
	}

	private void executeCRAFTTestIterations() {
		while (currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Iteration: "
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
			report.addTestLogSection("Iteration: "
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
		scriptHelper = new ScriptHelper(dataTable, report, driver, driverUtil,testParameters,dataBean);
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

				dataTable.setCurrentRow(testParameters.getCurrentTestcase(),
						currentIteration, currentSubIteration);

				if (currentSubIteration > 1) {
					report.addTestLogSubSection(currentKeyword
							+ " (Sub-Iteration: " + currentSubIteration + ")");
				} else {
					report.addTestLogSubSection(currentKeyword);
				}

				invokeBusinessComponent(currentKeyword);
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
			report.updateTestLog(exceptionName, exceptionDescription
					+ " <b>Caused by: </b>" + ex.getCause(), Status.FAIL);
		} else {
			report.updateTestLog(exceptionName, exceptionDescription,
					Status.FAIL);
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
				report.updateTestLog(
						"CRAFT Info",
						"Test case iteration terminated by user! Proceeding to next iteration (if applicable)...",
						Status.DONE);
				break;

			case NEXT_TESTCASE:
				report.updateTestLog(
						"CRAFT Info",
						"Test case terminated by user! Proceeding to next test case (if applicable)...",
						Status.DONE);
				currentIteration = testParameters.getEndIteration();
				break;

			case STOP:
				frameworkParameters.setStopExecution(true);
				report.updateTestLog(
						"CRAFT Info",
						"Test execution terminated by user! All subsequent tests aborted...",
						Status.DONE);
				currentIteration = testParameters.getEndIteration();
				break;

			default:
				throw new FrameworkException("Unhandled OnError option!");
			}
		}
	}

	private void quitWebDriver() {
		switch (testParameters.getExecutionMode()) {
		case LOCAL:
		case REMOTE:
		case LOCAL_EMULATED_DEVICE:
		case REMOTE_EMULATED_DEVICE:
		case GRID:
		case MOBILE:
		case MOBILELABS:
		case SAUCELABS:
		case PERFECTO:
		case MOBILECENTER:
		case BROWSERSTACK:
		case MINT:
			driver.quit();
			break;
		case SEETEST:
			// client.applicationClose(properties.getProperty("SeeTestAndroidApplicationName"));
			client.releaseDevice("*", true, false, true);
			seeTesResultPath = client.generateReport(true);
			client.releaseClient();
			driver.quit();
			break;
		default:
			throw new FrameworkException("Unhandled Execution Mode!");
		}

	}

	private boolean isMobileAutomation() {
		boolean isMobileAutomation = false;
		if (testParameters.getExecutionMode().equals(ExecutionMode.MOBILE)
				|| testParameters.getExecutionMode().equals(
						ExecutionMode.PERFECTO)
				|| testParameters.getExecutionMode().equals(
						ExecutionMode.SEETEST)
				|| testParameters.getExecutionMode().equals(
						ExecutionMode.MOBILELABS)
				|| testParameters.getExecutionMode().equals(
						ExecutionMode.SAUCELABS)
				|| testParameters.getExecutionMode().equals(
						ExecutionMode.BROWSERSTACK)
				|| testParameters.getExecutionMode().equals(
						ExecutionMode.MOBILECENTER)
				|| testParameters.getExecutionMode().equals(ExecutionMode.MINT)) {
			isMobileAutomation = true;
		}
		return isMobileAutomation;

	}

	@Override
	public void driveTestExecution(WebDriver driver) {
		// TODO Auto-generated method stub
		
	}
	
}