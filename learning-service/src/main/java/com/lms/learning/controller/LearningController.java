package com.lms.learning.controller;

import com.lms.learning.dto.*;
import com.lms.learning.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/sessions/resume")
    public SessionResponse resumeSession(@RequestHeader("X-User-Id") Long userId) {
        return learningService.resumeSession(userId);
    }

    @PostMapping("/sessions")
    public SessionResponse createSession(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody SessionRequest request) {
        return learningService.createSession(userId, request);
    }

    @PutMapping("/sessions/{sessionId}")
    public SessionResponse updateSession(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId,
            @RequestBody SessionRequest request) {
        return learningService.updateSession(userId, sessionId, request);
    }

    @GetMapping("/notes")
    public List<NoteResponse> myNotes(@RequestHeader("X-User-Id") Long userId) {
        return learningService.myNotes(userId);
    }

    @PostMapping("/notes")
    public NoteResponse createNote(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody NoteRequest request) {
        return learningService.createNote(userId, request);
    }

    @PutMapping("/notes/{noteId}")
    public NoteResponse updateNote(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long noteId,
            @RequestBody NoteRequest request) {
        return learningService.updateNote(userId, noteId, request);
    }

    @DeleteMapping("/notes/{noteId}")
    public void deleteNote(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long noteId) {
        learningService.deleteNote(userId, noteId);
    }

    @GetMapping("/bookmarks")
    public List<BookmarkResponse> myBookmarks(@RequestHeader("X-User-Id") Long userId) {
        return learningService.myBookmarks(userId);
    }

    @PostMapping("/bookmarks")
    public BookmarkResponse addBookmark(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody BookmarkRequest request) {
        return learningService.addBookmark(userId, request);
    }

    @DeleteMapping("/bookmarks/{bookmarkId}")
    public void deleteBookmark(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long bookmarkId) {
        learningService.deleteBookmark(userId, bookmarkId);
    }

    @GetMapping("/lessons/{lessonId}/qa")
    public List<QaResponse> lessonQa(@PathVariable Long lessonId) {
        return learningService.lessonQa(lessonId);
    }

    @PostMapping("/lessons/{lessonId}/qa")
    public QaResponse postQuestion(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long lessonId,
            @RequestBody QaRequest request) {
        return learningService.postQuestion(userId, lessonId, request);
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP", "service", "learning-service");
    }
}
