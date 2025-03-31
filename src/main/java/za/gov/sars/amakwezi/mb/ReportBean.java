/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.BarChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.common.ReportType;
import static za.gov.sars.amakhwezi.common.ReportType.CAPTURED_NOMINATIONS;
import static za.gov.sars.amakhwezi.common.ReportType.COST_CENTRE_REPORT;
import static za.gov.sars.amakhwezi.common.ReportType.PAYROLL_REPORT;
import za.gov.sars.amakhwezi.common.SarsValue;
import za.gov.sars.amakhwezi.common.Value;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.EmpNomination;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.mb.util.NominationHelper;
import za.gov.sars.amakhwezi.mb.util.CostCentreReportHelper;
import za.gov.sars.amakhwezi.mb.util.PayrollReportHelper;
import za.gov.sars.amakhwezi.mb.util.ReroutedReportHelper;
import za.gov.sars.amakhwezi.service.ContributionServiceLocal;
import za.gov.sars.amakhwezi.service.EmployeeServiceLocal;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;

/**
 *
 * @author S2026987
 */
@ManagedBean
@ViewScoped
public class ReportBean extends BaseBean {
    
    @Autowired
    private NominationServiceLocal nominationService;
    
    @Autowired
    private EmployeeServiceLocal employeeService;
    
    @Autowired
    private ContributionServiceLocal contributionService;
    
    private List<Nomination> nominations = new ArrayList();
    private List<Category> categories = new ArrayList<>();
    private List<Contribution> contributions = new ArrayList<>();
    private List<Integer> counterValues = new ArrayList<>();
    private List<PayrollReportHelper> payrollReports = new ArrayList<>();
    private List<CostCentreReportHelper> costCentreReports = new ArrayList<>();
    private List<PayrollReportHelper> selectedPayrollReports = new ArrayList<>();
    private List<NominationHelper> nominationReports = new ArrayList<>();
    private List<NominationHelper> selectedNominationReports = new ArrayList<>();
    private List<CostCentreReportHelper> selectedCentreReports = new ArrayList<>();
    private List<ReroutedReportHelper> reroutedReports = new ArrayList<>();
    private List<ReroutedReportHelper> selectedReroutedReports = new ArrayList<>();
    private List<ReportType> reportTypes = new ArrayList();
    private List<Value> values = new ArrayList<>();
    private List<SarsValue> sarsValues = new ArrayList<>();
    private Date startDate;
    private Date endDate;
    private Date payrollDate;
    private ReportType reportType;
    private BarChartModel barChart;
    
    private Double allocatedAmount = 0.00D;
    private Double availableAmount = 0.00D;
    private Double initialAmount = 0.00D;
    private Double usedAmount = 0.00D;
    private Integer numberofNominations = 0;
    
    private boolean viewRecievedNominations;
    private boolean viewCapturedNominations;
    private boolean viewBudgetVsActuals;
    private boolean viewNominationValues;
    private boolean viewNominationCategories;
    private boolean viewPayrollReport;
    private boolean viewSearchReport;
    private boolean viewCostCentreReport;
    private boolean viewReroutedReport;
    
    private StreamedContent file;
    
    private static final String[] columns = {"Nomination Head ID", "Capture Date", "Nominee Participant Code", "Nominee Sid", "Nominee Full Names",
        "Nominator Participant Code", "Nominator Sid", "Nominator Full Names", "Approver Participant Code", "Cost Center Manager Sid", "Finance Manager Sid", "Approver Full Names",
        "Region", "Functional Area", "Division", "Nomation Status", "Cost Center Status", "Finance Status", "Rand value", "Date updated", "Nomination Type", "Nomination Category", "Nomination Motivation",
        "Org Unit ID", "Org Unit Name", "CC Number", "CC Name", "OrgKey_Region Name"};
    
    @PostConstruct
    public void init() {
        this.resetViews().setViewSearchReport(true);
        reportTypes = Arrays.asList(ReportType.values());
    }
    
    public void viewReport() {
        nominations.clear();
        if (this.reportType.toString().isEmpty()) {
            addWarningMessage("Please select the report type");
            return;
        }
        
        if (this.startDate == null) {
            addWarningMessage("Please select the report start date range");
            return;
        }
        
        if (this.endDate == null) {
            addWarningMessage("Please select the report end date range");
            return;
        }
        
        if (this.endDate.before(this.startDate)) {
            addWarningMessage("The end date cannot be before the starting date please correct the date range selection");
            return;
        }

        //update the time to midnight for reporting
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DATE, 1);
        endDate = calendar.getTime();
        
        switch (this.reportType) {
            
            case CAPTURED_NOMINATIONS:
                
                setPanelTitleName(ReportType.CAPTURED_NOMINATIONS.toString());
                for (Nomination nom : nominationService.findCapturedNominations(startDate, endDate)) {
                    for (EmpNomination empNomination : nom.getEmployeeNominations()) {
                        NominationHelper nominationHelper = new NominationHelper();
                        nominationHelper.setId(RandomStringUtils.randomAlphanumeric(6));
                        nominationHelper.setNominee(empNomination.getEmployee());
                        nominationHelper.setNomination(empNomination.getNomination());
                        nominationHelper.setApprovedDate(nom.getUpdatedDate());
                        nominationReports.add(nominationHelper);
                    }
                }
                this.resetViews().setViewCapturedNominations(true);
                
                break;
            case PAYROLL_REPORT:
                setPanelTitleName(ReportType.PAYROLL_REPORT.toString());
                for (Nomination nomination1 : nominationService.findRecievedNominations(NominationStatus.APPROVED, startDate, endDate)) {
                    for (EmpNomination empNomination : nomination1.getEmployeeNominations()) {
                        PayrollReportHelper payrollReportHelper = new PayrollReportHelper();
                        payrollReportHelper.setId(RandomStringUtils.randomAlphanumeric(6));
                        payrollReportHelper.setReferenceId(nomination1.getReferenceId());
                        payrollReportHelper.setNominee(empNomination.getEmployee());
                        payrollReportHelper.setAmount(nomination1.getContribution().getAmount());
                        payrollReportHelper.setApprovedDate(nomination1.getUpdatedDate());
                        payrollReportHelper.setWageType("35G7");
                        payrollReportHelper.setRefferenceNo(nomination1.getUpdatedDate());
                        payrollReports.add(payrollReportHelper);
                    }
                }
                this.resetViews().setViewPayrollReport(true);
                break;
            case COST_CENTRE_REPORT:
                //   nominations.clear();
                //   contributions.clear();
                setPanelTitleName(ReportType.COST_CENTRE_REPORT.toString());
                //   contributions = contributionService.listAll();
                for (Nomination nomination2 : nominationService.findRecievedNominations(NominationStatus.APPROVED, startDate, endDate)) {
                    for (EmpNomination empNomination : nomination2.getEmployeeNominations()) {
                        CostCentreReportHelper costCentreReportHelper = new CostCentreReportHelper();
                        costCentreReportHelper.setReferenceId(nomination2.getReferenceId());
                        costCentreReportHelper.setNominee(empNomination.getEmployee());
                        costCentreReportHelper.setCreatedDate(empNomination.getCreatedDate());
                        costCentreReportHelper.setApprovedDate(empNomination.getNomination().getUpdatedDate());
                        costCentreReportHelper.setContribution(empNomination.getNomination().getContribution());
                        costCentreReportHelper.setNominator(empNomination.getNomination().getNominator().getEmployee());
                        costCentreReports.add(costCentreReportHelper);
                    }
                }
                this.resetViews().setViewCostCentreReport(true);
                break;
            
            case REROUTED_NOMINATIONS_REPORT:
                //   nominations.clear();
                //   contributions.clear();
                setPanelTitleName(ReportType.REROUTED_NOMINATIONS_REPORT.toString());
                //   contributions = contributionService.listAll();
                for (Nomination nomination3 : nominationService.findReroutedNominations(startDate, endDate)) {
                    for (EmpNomination empNomination : nomination3.getEmployeeNominations()) {
                        ReroutedReportHelper reroutedReportHelper = new ReroutedReportHelper();
                        reroutedReportHelper.setReferenceId(nomination3.getReferenceId());
                        reroutedReportHelper.setNominee(empNomination.getEmployee());
                        reroutedReportHelper.setCreatedDate(empNomination.getCreatedDate());
                        reroutedReportHelper.setApprovedDate(empNomination.getNomination().getUpdatedDate());
                        reroutedReportHelper.setNomination(empNomination.getNomination());
                        reroutedReportHelper.setRerouterSid(empNomination.getNomination().getRerouterSid());
                        reroutedReportHelper.setNominator(empNomination.getNomination().getNominator().getEmployee());
                        reroutedReports.add(reroutedReportHelper);
                    }
                }
                this.resetViews().setViewReroutedReport(true);
                break;
        }
    }
    
    public ReportBean resetViews() {
        setViewBudgetVsActuals(false);
        setViewCapturedNominations(false);
        setViewNominationCategories(false);
        setViewNominationValues(false);
        setViewRecievedNominations(false);
        setViewSearchReport(false);
        setViewPayrollReport(false);
        setViewCostCentreReport(false);
        setViewReroutedReport(false);
        return this;
    }

//    public void createBarChart() {
//        barChart = budgetVsActualChart();
//        barChart.setTitle("Budget Vs Actual");
//        barChart.setLegendPosition("ne");
//
//        Axis xAxis = barChart.getAxis(AxisType.X);
//        xAxis.setLabel(startDate + " - " + endDate);
//
//        Axis yAxis = barChart.getAxis(AxisType.Y);
//        yAxis.setLabel("Amount");
//        yAxis.setMin(0.00D);
//        yAxis.setMax(allocatedAmount + 100000.00D);
//    }
//    public BarChartModel budgetVsActualChart() {
//        BarChartModel model = new BarChartModel();
//        for (Employee emp : employeeService.findBudgetByStartDateAndEndDate(startDate, endDate)) {
//            allocatedAmount += emp.getAvailableAmount() + emp.getUsedAmount();
//            availableAmount += emp.getAvailableAmount();
//            initialAmount += emp.getAmount();
//            usedAmount += emp.getUsedAmount();
//        }
//
//        ChartSeries initAmount = new ChartSeries();
//        initAmount.setLabel("Initial Amount");
//
//        ChartSeries allAmount = new ChartSeries();
//        allAmount.setLabel("Allocated Amount");
//
//        ChartSeries useAmount = new ChartSeries();
//        useAmount.setLabel("Used  Amount");
//
//        ChartSeries avAmount = new ChartSeries();
//        avAmount.setLabel("Available Amount");
//
//        initAmount.set("", initialAmount);
//        allAmount.set("", allocatedAmount);
//        useAmount.set("", usedAmount);
//        avAmount.set("", availableAmount);
//
//        model.addSeries(initAmount);
//        model.addSeries(allAmount);
//        model.addSeries(useAmount);
//        model.addSeries(avAmount);
//
//        return model;
//
//    }
    public void downloadNominationReport() {
        Workbook workbook = new HSSFWorkbook(); //reportType
        Sheet sheet = workbook.createSheet("Nominations");
        sheet.setDefaultColumnStyle(0, workbook.createCellStyle());
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerFont.setBold(true);
        headerFont.setItalic(false);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(BorderStyle.THICK);
        headerCellStyle.setBorderLeft(BorderStyle.THICK);
        headerCellStyle.setBorderRight(BorderStyle.THICK);
        headerCellStyle.setBorderTop(BorderStyle.THICK);
        Row headerRow = sheet.createRow(0);
        
        int counter = 0;
        for (String column : columns) {
            Cell cell = headerRow.createCell(counter);
            cell.setCellValue(column);
            cell.setCellStyle(headerCellStyle);
            
            counter++;
        }
        
        int rownum = 1;
        for (Nomination nomination : nominations) {
            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                Row row = sheet.createRow(rownum++);
                row.createCell(0).setCellValue(nomination.getReferenceId()); //Nomination Head ID
                row.createCell(1).setCellValue(convertStringToDate(nomination.getCreatedDate())); //Capture Date
                row.createCell(2).setCellValue(empNomination.getEmployee().getEmpDetails().getPersonnelNum()); //Nominee Participant Code
                row.createCell(3).setCellValue(empNomination.getEmployee().getEmployeeSid()); //Nominee Sid
                row.createCell(4).setCellValue(empNomination.getEmployee().getEmpDetails().getFullnames()); //Nominee Full Names
                row.createCell(5).setCellValue(nomination.getNominator().getEmployee().getEmpDetails().getPersonnelNum()); //Nominator Participant Code
                row.createCell(6).setCellValue(nomination.getNominator().getEmployee().getEmployeeSid()); //Nominator Sid
                row.createCell(7).setCellValue(nomination.getNominator().getEmployee().getEmpDetails().getFullnames()); //Nominator Full Names
                row.createCell(8).setCellValue(nomination.getCostCentreEmployeeNumber()); //Approver Participant Code
                row.createCell(9).setCellValue(nomination.getCostCenterManagerSid()); //Cost Centre Manager Sid
                row.createCell(10).setCellValue(nomination.getFinanceManagerSid()); //finance Manager Sid
                row.createCell(11).setCellValue(nomination.getCostCenterManagerFullnames()); //Approver Full Names
                row.createCell(12).setCellValue(empNomination.getEmployee().getEmpDetails().getOrgKey()); //Region
                row.createCell(13).setCellValue(empNomination.getEmployee().getEmpDetails().getPersonnelArea()); //Functional Area
                row.createCell(14).setCellValue(empNomination.getEmployee().getEmpDetails().getPersonnelSubArea()); //Division
                row.createCell(15).setCellValue(nomination.getNominationStatus().toString()); //Nomination Status
                if (nomination.getCostCentreManagerStatus() != null) {
                    row.createCell(16).setCellValue(nomination.getCostCentreManagerStatus().toString()); //Cost Center Manager Status
                } else {
                    row.createCell(16).setCellValue(""); //Cost Center Manager Status
                }
                if (nomination.getFinanceManagerStatus() != null) {
                    row.createCell(17).setCellValue(nomination.getFinanceManagerStatus().toString()); //Finance Manager Status
                } else {
                    row.createCell(17).setCellValue(""); //Cost Center Manager Status
                }
                row.createCell(18).setCellValue(nomination.getContribution().getAmount()); //Rand value
                if (!nomination.getNominationStatus().equals(NominationStatus.SUBMITTED) || !nomination.getNominationStatus().equals(NominationStatus.SAVED)) {
                    row.createCell(19).setCellValue(convertStringToDate(nomination.getUpdatedDate())); //Date updated
                } else {
                    row.createCell(19).setCellValue(""); //Date updated
                }
                row.createCell(20).setCellValue(nomination.getNominationType().toString()); //Nomination Type
                row.createCell(21).setCellValue(nomination.getCategory().getDescription()); //Nomination Category              
                row.createCell(22).setCellValue(nomination.getMotivation()); //Nomination Motivation
                row.createCell(23).setCellValue(empNomination.getEmployee().getEmpDetails().getOrgUnitId()); //Org Unit ID
                row.createCell(24).setCellValue(empNomination.getEmployee().getEmpDetails().getOrgUnitName()); //Org Unit Name
                row.createCell(25).setCellValue(empNomination.getEmployee().getEmpDetails().getCostCenterNumber()); //CC Number
                row.createCell(26).setCellValue(empNomination.getEmployee().getEmpDetails().getCostCenterName()); //CC Name
                row.createCell(27).setCellValue(empNomination.getEmployee().getEmpDetails().getOrgKeyName()); //OrgKey_Region Name
            }
        }

        // Resize all columns to fit the content size
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        try {
            //FileOutputStream fileOut = new FileOutputStream("D:\\Users\\s2026987\\Downloads\\Profiles.xls");
            //workbook.write(fileOut);
            //fileOut.close();
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            workbook.write(outputStream);
            
            byte[] outArray = outputStream.toByteArray();
            
            response.reset();
            response.setContentType("application/ms-excel");
            response.setContentLength(outArray.length);
            response.setHeader("Expires:", "0"); // eliminates browser caching
            response.setHeader("Content-Disposition", "attachment; filename=" + this.reportType.toString() + ".xls");
            
            OutputStream outStream = response.getOutputStream();
            outStream.write(outArray);
            outStream.flush();
            
            fc.responseComplete();
            //workbook.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReportBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        addInformationMessage("Nominations document has been downloaded successfully");
    }
    
    public void processPayrollDate() {
        payrollReports.clear();
        for (PayrollReportHelper payrollReportHelper : selectedPayrollReports) {
            payrollReportHelper.setApprovedDate(payrollDate);
            payrollReportHelper.setRefferenceNo(payrollDate);
            payrollReports.add(payrollReportHelper);
        }
    }
    
    public List<Nomination> getNominations() {
        return nominations;
    }
    
    public void setNominations(List<Nomination> nominations) {
        this.nominations = nominations;
    }
    
    public List<ReportType> getReportTypes() {
        return reportTypes;
    }
    
    public void setReportTypes(List<ReportType> reportTypes) {
        this.reportTypes = reportTypes;
    }
    
    public boolean isViewRecievedNominations() {
        return viewRecievedNominations;
    }
    
    public void setViewRecievedNominations(boolean viewRecievedNominations) {
        this.viewRecievedNominations = viewRecievedNominations;
    }
    
    public boolean isViewCapturedNominations() {
        return viewCapturedNominations;
    }
    
    public void setViewCapturedNominations(boolean viewCapturedNominations) {
        this.viewCapturedNominations = viewCapturedNominations;
    }
    
    public boolean isViewBudgetVsActuals() {
        return viewBudgetVsActuals;
    }
    
    public void setViewBudgetVsActuals(boolean viewBudgetVsActuals) {
        this.viewBudgetVsActuals = viewBudgetVsActuals;
    }
    
    public boolean isViewNominationValues() {
        return viewNominationValues;
    }
    
    public void setViewNominationValues(boolean viewNominationValues) {
        this.viewNominationValues = viewNominationValues;
    }
    
    public boolean isViewNominationCategories() {
        return viewNominationCategories;
    }
    
    public void setViewNominationCategories(boolean viewNominationCategories) {
        this.viewNominationCategories = viewNominationCategories;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public ReportType getReportType() {
        return reportType;
    }
    
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
    
    public boolean isViewSearchReport() {
        return viewSearchReport;
    }
    
    public void setViewSearchReport(boolean viewSearchReport) {
        this.viewSearchReport = viewSearchReport;
    }
    
    public List<Category> getCategories() {
        return categories;
    }
    
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
    
    public List<Integer> getCounterValues() {
        return counterValues;
    }
    
    public void setCounterValues(List<Integer> counterValues) {
        this.counterValues = counterValues;
    }
    
    public List<Value> getValues() {
        return values;
    }
    
    public void setValues(List<Value> values) {
        this.values = values;
    }
    
    public List<SarsValue> getSarsValues() {
        return sarsValues;
    }
    
    public void setSarsValues(List<SarsValue> sarsValues) {
        this.sarsValues = sarsValues;
    }
    
    public BarChartModel getBarChart() {
        return barChart;
    }
    
    public void setBarChart(BarChartModel barChart) {
        this.barChart = barChart;
    }
    
    public Double getAllocatedAmount() {
        return allocatedAmount;
    }
    
    public void setAllocatedAmount(Double allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }
    
    public Double getAvailableAmount() {
        return availableAmount;
    }
    
    public void setAvailableAmount(Double availableAmount) {
        this.availableAmount = availableAmount;
    }
    
    public Integer getNumberofNominations() {
        return numberofNominations;
    }
    
    public void setNumberofNominations(Integer numberofNominations) {
        this.numberofNominations = numberofNominations;
    }
    
    public List<PayrollReportHelper> getPayrollReports() {
        return payrollReports;
    }
    
    public void setPayrollReports(List<PayrollReportHelper> payrollReports) {
        this.payrollReports = payrollReports;
    }
    
    public List<NominationHelper> getNominationReports() {
        return nominationReports;
    }
    
    public void setNominationReports(List<NominationHelper> nominationReports) {
        this.nominationReports = nominationReports;
    }
    
    public Double getInitialAmount() {
        return initialAmount;
    }
    
    public void setInitialAmount(Double initialAmount) {
        this.initialAmount = initialAmount;
    }
    
    public Double getUsedAmount() {
        return usedAmount;
    }
    
    public void setUsedAmount(Double usedAmount) {
        this.usedAmount = usedAmount;
    }
    
    public boolean isViewPayrollReport() {
        return viewPayrollReport;
    }
    
    public void setViewPayrollReport(boolean viewPayrollReport) {
        this.viewPayrollReport = viewPayrollReport;
    }
    
    public StreamedContent getFile() {
        return file;
    }
    
    public void setFile(StreamedContent file) {
        this.file = file;
    }
    
    public Date getPayrollDate() {
        return payrollDate;
    }
    
    public void setPayrollDate(Date payrollDate) {
        this.payrollDate = payrollDate;
    }
    
    public List<PayrollReportHelper> getSelectedPayrollReports() {
        return selectedPayrollReports;
    }
    
    public void setSelectedPayrollReports(List<PayrollReportHelper> selectedPayrollReports) {
        this.selectedPayrollReports = selectedPayrollReports;
    }
    
    public boolean isViewCostCentreReport() {
        return viewCostCentreReport;
    }
    
    public void setViewCostCentreReport(boolean viewCostCentreReport) {
        this.viewCostCentreReport = viewCostCentreReport;
    }
    
    public List<Contribution> getContributions() {
        return contributions;
    }
    
    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }
    
    public List<CostCentreReportHelper> getCostCentreReports() {
        return costCentreReports;
    }
    
    public void setCostCentreReports(List<CostCentreReportHelper> costCentreReports) {
        this.costCentreReports = costCentreReports;
    }
    
    public List<NominationHelper> getSelectedNominationReports() {
        return selectedNominationReports;
    }
    
    public void setSelectedNominationReports(List<NominationHelper> selectedNominationReports) {
        this.selectedNominationReports = selectedNominationReports;
    }
    
    public List<ReroutedReportHelper> getReroutedReports() {
        return reroutedReports;
    }
    
    public void setReroutedReports(List<ReroutedReportHelper> reroutedReports) {
        this.reroutedReports = reroutedReports;
    }
    
    public List<ReroutedReportHelper> getSelectedReroutedReports() {
        return selectedReroutedReports;
    }
    
    public void setSelectedReroutedReports(List<ReroutedReportHelper> selectedReroutedReports) {
        this.selectedReroutedReports = selectedReroutedReports;
    }
    
    public boolean isViewReroutedReport() {
        return viewReroutedReport;
    }
    
    public void setViewReroutedReport(boolean viewReroutedReport) {
        this.viewReroutedReport = viewReroutedReport;
    }
    
}
