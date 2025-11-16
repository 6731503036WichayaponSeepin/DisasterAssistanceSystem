package th.mfu.controller.rescuecontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.rescue.AffiliatedUnit;
import th.mfu.repository.rescuerepository.AffiliatedUnitRepository;

@RestController
@RequestMapping("/api/units")
@CrossOrigin
public class AffiliatedUnitController {

    @Autowired private AffiliatedUnitRepository unitRepo;

    @PostMapping
    public AffiliatedUnit createUnit(@RequestBody AffiliatedUnit unit) {
        return unitRepo.save(unit);
    }

    @GetMapping
    public List<AffiliatedUnit> getAllUnits() {
        return unitRepo.findAll();
    }
}
