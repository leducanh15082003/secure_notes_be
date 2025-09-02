package be.spring.secure_notes.controller;

import be.spring.secure_notes.model.Note;
import be.spring.secure_notes.model.User;
import be.spring.secure_notes.repository.NoteRepository;
import be.spring.secure_notes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NotesController {
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Note> getNotes(Authentication authentication) {
        String username = authentication.getName();
        return noteRepository.findByOwnerUsername(username);
    }

    @PostMapping
    public Note createNote(Authentication authentication, @RequestBody Note note) {
        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        note.setOwner(owner);
        return noteRepository.save(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody Note updatedNote
    ) {
        return noteRepository.findById(id)
                .filter(note -> note.getOwner().getUsername().equals(authentication.getName()))
                .<ResponseEntity<?>>map(note -> { // ép kiểu ở đây
                    note.setTitle(updatedNote.getTitle());
                    note.setContent(updatedNote.getContent());
                    noteRepository.save(note);
                    return ResponseEntity.ok(note);
                })
                .orElse(ResponseEntity.status(403).body("Access denied"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return noteRepository.findById(id)
                .filter(note -> note.getOwner().getUsername().equals(authentication.getName()))
                .map(note -> {
                    noteRepository.delete(note);
                    return ResponseEntity.ok("Note deleted");
                })
                .orElse(ResponseEntity.status(403).body("Access denied"));
    }
}
