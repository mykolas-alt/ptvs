package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.audit.AuditAction;
import lt.pskurimas.ptvs.audit.Auditable;
import lt.pskurimas.ptvs.dto.request.reports.ServiceReportRequest;
import lt.pskurimas.ptvs.dto.response.PagedResponse;
import lt.pskurimas.ptvs.dto.response.reports.CostReportSummary;
import lt.pskurimas.ptvs.dto.response.reports.ServiceReportResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.service.ReportService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("cost-report")
    @Auditable(action = AuditAction.GENERATE_COST_REPORT, payloadType = ServiceReportRequest.class)
    public ResponseEntity<ServiceReportResponse> generateReport(
            @RequestBody ServiceReportRequest request,
            @CurrentUser AppUser user) {
        return ResponseEntity.ok(reportService.generateCostReport(request));
    }

    @GetMapping("cost-report")
    public PagedResponse<CostReportSummary> getAllReports(@CurrentUser AppUser user,
            @PageableDefault Pageable pageable) {
        return PagedResponse.of(reportService.getAllSavedReports(pageable));
    }

    @GetMapping("cost-report/{id}")
    public ResponseEntity<ServiceReportResponse> getReportById(
            @PathVariable UUID id,
            @CurrentUser AppUser user) {
        return ResponseEntity.ok(reportService.getSavedReportById(id));
    }
}