package lt.pskurimas.ptvs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.dto.request.ServiceReportRequest;
import lt.pskurimas.ptvs.dto.response.ServiceReportResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.service.ReportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<ServiceReportResponse> generateReport(
            @RequestBody ServiceReportRequest request,
            @CurrentUser AppUser user) {

        return ResponseEntity.ok(reportService.generateCostReport(request));
    }
}