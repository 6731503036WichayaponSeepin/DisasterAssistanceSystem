package th.mfu.controller.casecontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.service.CaseSelectionService;

@RestController
@RequestMapping("/api/case-selection")
@CrossOrigin
public class CaseSelectionController {

    private CaseSelectionService selectionService;

    @Autowired
    public CaseSelectionController(CaseSelectionService selectionService) {
        this.selectionService = selectionService;
    }

    /**
     * ดึงเคสทั้งหมดที่ยังไม่มีทีมรับ
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCases() {
        return ResponseEntity.ok(selectionService.getAvailableCases());
    }

    /**
     * ดึงเคสตามประเภท (case_type): SOS / SUSTENANCE
     */
    @GetMapping("/type/{caseType}")
    public ResponseEntity<?> getCasesByType(@PathVariable String caseType) {
        return ResponseEntity.ok(selectionService.getAvailableCasesByType(caseType));
    }
}