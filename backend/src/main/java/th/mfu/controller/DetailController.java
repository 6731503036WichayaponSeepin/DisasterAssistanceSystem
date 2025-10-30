package th.mfu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import th.mfu.model.Detail;
import th.mfu.repository.DetailRepository;

import java.util.List;

@RestController
@RequestMapping("/api/details")
@CrossOrigin
public class DetailController {

    @Autowired
    private DetailRepository detailRepo;

    @GetMapping
    public List<Detail> all() {
        return detailRepo.findAll();
    }

    @PostMapping
    public Detail create(@RequestBody Detail input) {
        return detailRepo.save(input);
    }
}
