package th.mfu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.Detail;
import th.mfu.repository.DetailRepository;

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
