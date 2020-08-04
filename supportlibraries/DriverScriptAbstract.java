package supportlibraries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.cognizant.framework.CraftDataTable;
import com.cognizant.framework.DataBean;
import com.cognizant.framework.ExcelDataAccess;
import com.cognizant.framework.FrameworkException;
import com.cognizant.framework.FrameworkParameters;
import com.cognizant.framework.IterationOptions;
import com.cognizant.framework.ReportSettings;
import com.cognizant.framework.Settings;
import com.cognizant.framework.Util;
import com.cognizant.framework.selenium.Browser;
import com.cognizant.framework.selenium.CraftDriver;
import com.cognizant.framework.selenium.ExecutionMode;
import com.cognizant.framework.selenium.MobileExecutionPlatform;
import com.cognizant.framework.selenium.MobileToolName;
import com.cognizant.framework.selenium.RemoteWebDriverUtils;
import com.cognizant.framework.selenium.SeleniumReport;
import com.cognizant.framework.selenium.SeleniumTestParameters;
import com.cognizant.framework.selenium.WebDriverUtil;
import com.experitest.client.Client;

/**
 * Driver script class which encapsulates the core logic of the framework
 * 
 * @author Cognizant
 */
public abstract class DriverScriptAbstract {

	protected List<String> businessFlowData;
	protected int currentIteration, currentSubIteration;
	protected Date startTime, endTime;
	protected String executionTime;

	protected CraftDataTable dataTable;
	protected ReportSettings reportSettings;
	protected SeleniumReport report;

	protected CraftDriver driver;

	protected WebDriverUtil driverUtil;
	protected ScriptHelper scriptHelper;
	protected Client client;

	protected Properties properties;
	protected Properties mobileProperties;
	protected final FrameworkParameters frameworkParameters = FrameworkParameters
			.getInstance();

	protected Boolean linkScreenshotsToTestLog = true;

	protected final SeleniumTestParameters testParameters;
	protected String reportPath;

	protected CRAFTLiteTestCase testCase;
	protected String seeTesResultPath;
	protected DataBean dataBean = new DataBean();

	/**
	 * DriverScript constructor
	 * 
	 * @param testParameters
	 *            A {@link SeleniumTestParameters} object
	 */
	public DriverScriptAbstract(SeleniumTestParameters testParameters) {
		this.testParameters = testParameters;
	}

	/**
	 * Function to configure the linking of screenshots to the corresponding
	 * test log
	 * 
	 * @param linkScreenshotsToTestLog
	 *            Boolean variable indicating whether screenshots should be
	 *            linked to the corresponding test log
	 */
	public void setLinkScreenshotsToTestLog(Boolean linkScreenshotsToTestLog) {
		this.linkScreenshotsToTestLog = linkScreenshotsToTestLog;
	}

	/**
	 * Function to get the name of the test report
	 * 
	 * @return The test report name
	 */
	public String getReportName() {
		return reportSettings.getReportName();
	}

	/**
	 * Function to get the status of the test case executed
	 * 
	 * @return The test status
	 */
	public String getTestStatus() {
		return report.getTestStatus();
	}

	/**
	 * Function to get the decription of any failure that may occur during the
	 * script execution
	 * 
	 * @return The failure description (relevant only if the test fails)
	 */
	public String getFailureDescription() {
		return report.getFailureDescription();
	}

	/**
	 * Function to get the execution time for the test case
	 * 
	 * @return The test execution time
	 */
	public String getExecutionTime() {
		return executionTime;
	}

	public abstract void driveTestExecution();
	
	public abstract void driveTestExecution(WebDriver driver);

	protected void startUp() {
		startTime = Util.getCurrentTime();

		properties = Settings.getInstance();
		mobileProperties = Settings.getMobilePropertiesInstance();

		setDefaultTestParameters();
	}

	protected void setDefaultTestParameters() {
		if (testParameters.getIterationMode() == null) {
			testParameters
					.setIterationMode(IterationOptions.RUN_ALL_ITERATIONS);
		}

		if (testParameters.getExecutionMode() == null) {
			testParameters.setExecutionMode(ExecutionMode.valueOf(properties
					.getProperty("DefaultExecutionMode")));
		}

		if (testParameters.getMobileExecutionPlatform() == null) {
			testParameters.setMobileExecutionPlatform(MobileExecutionPlatform
					.valueOf(mobileProperties
							.getProperty("DefaultMobileExecutionPlatform")));
		}

		if (testParameters.getMobileToolName() == null) {
			testParameters.setMobileToolName(MobileToolName
					.valueOf(mobileProperties
							.getProperty("DefaultMobileToolName")));
		}

		if (testParameters.getDeviceName() == null) {
			testParameters.setDeviceName(mobileProperties
					.getProperty("DefaultDevice"));
		}

		if (testParameters.getBrowser() == null) {
			testParameters.setBrowser(Browser.valueOf(properties
					.getProperty("DefaultBrowser")));
		}

		if (testParameters.getPlatform() == null) {
			testParameters.setPlatform(Platform.valueOf(properties
					.getProperty("DefaultPlatform")));
		}

		if (testParameters.getSeeTestPort() == null) {
			testParameters.setSeeTestPort(mobileProperties
					.getProperty("SeeTestDefaultPort"));
		}

		testParameters.setInstallApplication(Boolean
				.parseBoolean(mobileProperties
						.getProperty("InstallApplicationInDevice")));

	}

	protected void initializeTestIterations() {
		switch (testParameters.getIterationMode()) {
		case RUN_ALL_ITERATIONS:
			int nIterations = getNumberOfIterations();
			testParameters.setEndIteration(nIterations);

			currentIteration = 1;
			break;

		case RUN_ONE_ITERATION_ONLY:
			currentIteration = 1;
			break;

		case RUN_RANGE_OF_ITERATIONS:
			if (testParameters.getStartIteration() > testParameters
					.getEndIteration()) {
				throw new FrameworkException("Error",
						"StartIteration cannot be greater than EndIteration!");
			}
			currentIteration = testParameters.getStartIteration();
			break;

		default:
			throw new FrameworkException("Unhandled Iteration Mode!");
		}
	}

	protected int getNumberOfIterations() {
		String datatablePath = frameworkParameters.getRelativePath()
				+ Util.getFileSeparator() + "Datatables";
		ExcelDataAccess testDataAccess = new ExcelDataAccess(datatablePath,
				testParameters.getCurrentScenario());
		testDataAccess.setDatasheetName(properties
				.getProperty("DefaultDataSheet"));
		if (properties.getProperty("Approach")
				.equalsIgnoreCase("KeywordDriven")) {
			int startRowNum = testDataAccess.getRowNum(
					testParameters.getCurrentTestcase(), 0);
			int nTestcaseRows = testDataAccess.getRowCount(
					testParameters.getCurrentTestcase(), 0, startRowNum);
			int nSubIterations = testDataAccess
					.getRowCount("1", 1, startRowNum); // Assumption:
														// Every
														// test
														// case
														// will
														// have
														// at
														// least
														// one
														// iteration
			return nTestcaseRows / nSubIterations;
		} else {
			return testDataAccess.getRowCount(
					testParameters.getCurrentTestcase(), 0);
		}

	}

	protected synchronized void initializeDatatable() {
		String datatablePath = frameworkParameters.getRelativePath()
				+ Util.getFileSeparator() + "Datatables";

		String runTimeDatatablePath;
		Boolean includeTestDataInReport = Boolean.parseBoolean(properties
				.getProperty("IncludeTestDataInReport"));
		if (includeTestDataInReport) {
			runTimeDatatablePath = reportPath + Util.getFileSeparator()
					+ "Datatables";

			File runTimeDatatable = new File(runTimeDatatablePath
					+ Util.getFileSeparator()
					+ testParameters.getCurrentScenario() + ".xls");
			if (!runTimeDatatable.exists()) {
				File datatable = new File(datatablePath
						+ Util.getFileSeparator()
						+ testParameters.getCurrentScenario() + ".xls");

				try {
					FileUtils.copyFile(datatable, runTimeDatatable);
				} catch (IOException e) {
					e.printStackTrace();
					throw new FrameworkException(
							"Error in creating run-time datatable: Copying the datatable failed...");
				}
			}

			File runTimeCommonDatatable = new File(runTimeDatatablePath
					+ Util.getFileSeparator() + "Common Testdata.xls");
			if (!runTimeCommonDatatable.exists()) {
				File commonDatatable = new File(datatablePath
						+ Util.getFileSeparator() + "Common Testdata.xls");

				try {
					FileUtils.copyFile(commonDatatable, runTimeCommonDatatable);
				} catch (IOException e) {
					e.printStackTrace();
					throw new FrameworkException(
							"Error in creating run-time datatable: Copying the common datatable failed...");
				}
			}
		} else {
			runTimeDatatablePath = datatablePath;
		}

		dataTable = new CraftDataTable(runTimeDatatablePath,
				testParameters.getCurrentScenario());
		dataTable.setDataReferenceIdentifier(properties
				.getProperty("DataReferenceIdentifier"));
		// CRAFTLite Change
		if (properties.getProperty("Approach")
				.equalsIgnoreCase("ModularDriven")) {
			// Initialize the datatable row in case test data is required during
			// the setUp()
			dataTable.setCurrentRow(testParameters.getCurrentTestcase(),
					currentIteration, 0);
		}
	}

	protected void initializeTestScript() {
		driverUtil = new WebDriverUtil(driver);
		scriptHelper = new ScriptHelper(dataTable, report, driver, driverUtil,testParameters,dataBean);
		driver.setRport(report);
		initializeBusinessFlow();
	}

	protected void initializeBusinessFlow() {
		ExcelDataAccess businessFlowAccess = new ExcelDataAccess(
				frameworkParameters.getRelativePath() + Util.getFileSeparator()
						+ "Datatables", testParameters.getCurrentScenario());
		businessFlowAccess.setDatasheetName("Business_Flow");

		int rowNum = businessFlowAccess.getRowNum(
				testParameters.getCurrentTestcase(), 0);
		if (rowNum == -1) {
			throw new FrameworkException("The test case \""
					+ testParameters.getCurrentTestcase()
					+ "\" is not found in the Business Flow sheet!");
		}

		String dataValue;
		businessFlowData = new ArrayList<String>();
		int currentColumnNum = 1;
		while (true) {
			dataValue = businessFlowAccess.getValue(rowNum, currentColumnNum);
			if ("".equals(dataValue)) {
				break;
			}
			businessFlowData.add(dataValue);
			currentColumnNum++;
		}

		if (businessFlowData.isEmpty()) {
			throw new FrameworkException(
					"No business flow found against the test case \""
							+ testParameters.getCurrentTestcase() + "\"");
		}
	}

	protected void wrapUp() {
		endTime = Util.getCurrentTime();
		closeTestReport();
		downloadAddtionalReport();
	}

	protected void closeTestReport() {
		executionTime = Util.getTimeDifference(startTime, endTime);
		report.addTestLogFooter(executionTime);

		if (reportSettings.shouldConsolidateScreenshotsInWordDoc()) {
			report.consolidateScreenshotsInWordDoc();
		}

	}

	protected void downloadAddtionalReport() {
		if (testParameters.getExecutionMode().equals(ExecutionMode.PERFECTO)
				&& reportSettings.shouldGeneratePerfectoReports()
				&& testParameters.getMobileToolName().equals(
						MobileToolName.DEFAULT)) {
			try {
				driver.close();
				RemoteWebDriverUtils.downloadReport(
						(RemoteWebDriver) driver.getWebDriver(), "pdf",
						reportPath + Util.getFileSeparator()
								+ "Perfecto Results" + Util.getFileSeparator()
								+ testParameters.getCurrentScenario() + "_"
								+ testParameters.getCurrentTestcase() + "_"
								+ testParameters.getCurrentTestInstance()
								+ ".pdf");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (testParameters.getExecutionMode().equals(ExecutionMode.SEETEST)
				&& reportSettings.shouldGenerateSeeTestReports()) {
			new File(reportPath + Util.getFileSeparator() + "SeeTest Results"
					+ Util.getFileSeparator()
					+ testParameters.getCurrentTestcase() + "_"
					+ testParameters.getCurrentTestInstance()).mkdir();
			File source = new File(seeTesResultPath);
			File dest = new File(reportPath + Util.getFileSeparator()
					+ "SeeTest Results" + Util.getFileSeparator()
					+ testParameters.getCurrentTestcase() + "_"
					+ testParameters.getCurrentTestInstance());

			try {
				FileUtils.copyDirectoryToDirectory(source, dest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}