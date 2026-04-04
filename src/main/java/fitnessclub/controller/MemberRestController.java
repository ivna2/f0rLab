package fitnessclub.controller;

import fitnessclub.dto.MemberRequest;
import fitnessclub.model.Member;
import fitnessclub.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<Member>> getAll() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> get(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.get(id));
    }

    @PostMapping
    public ResponseEntity<Member> add(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(CREATED).body(memberService.add(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
