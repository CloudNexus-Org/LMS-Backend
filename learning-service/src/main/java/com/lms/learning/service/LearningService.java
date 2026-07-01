package com.lms.learning.service;

import com.lms.learning.dto.*;
import com.lms.learning.model.*;
import com.lms.learning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningService {

    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LearningSessionRepository sessionRepository;
    private final LessonQaRepository lessonQaRepository;

    public SessionResponse resumeSession(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .findFirst()
                .map(SessionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No learning session found"));
    }

    @Transactional
    public SessionResponse createSession(Long userId, SessionRequest request) {
        if (request.getTrackId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trackId is required");
        }
        LearningSession session = sessionRepository.save(LearningSession.builder()
                .userId(userId)
                .trackId(request.getTrackId())
                .lastLessonId(request.getLastLessonId())
                .lastPositionSec(request.getLastPositionSec() != null ? request.getLastPositionSec() : 0)
                .build());
        return SessionResponse.from(session);
    }

    @Transactional
    public SessionResponse updateSession(Long userId, Long sessionId, SessionRequest request) {
        LearningSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (request.getTrackId() != null) session.setTrackId(request.getTrackId());
        if (request.getLastLessonId() != null) session.setLastLessonId(request.getLastLessonId());
        if (request.getLastPositionSec() != null) session.setLastPositionSec(request.getLastPositionSec());
        return SessionResponse.from(sessionRepository.save(session));
    }

    public List<NoteResponse> myNotes(Long userId) {
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(NoteResponse::from)
                .toList();
    }

    @Transactional
    public NoteResponse createNote(Long userId, NoteRequest request) {
        if (request.getLessonId() == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lessonId and content are required");
        }
        Note note = noteRepository.save(Note.builder()
                .userId(userId)
                .lessonId(request.getLessonId())
                .trackId(request.getTrackId())
                .content(request.getContent())
                .build());
        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse updateNote(Long userId, Long noteId, NoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        if (request.getContent() != null) note.setContent(request.getContent());
        if (request.getTrackId() != null) note.setTrackId(request.getTrackId());
        return NoteResponse.from(noteRepository.save(note));
    }

    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        noteRepository.delete(note);
    }

    public List<BookmarkResponse> myBookmarks(Long userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BookmarkResponse::from)
                .toList();
    }

    @Transactional
    public BookmarkResponse addBookmark(Long userId, BookmarkRequest request) {
        if (request.getLessonId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lessonId is required");
        }
        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .userId(userId)
                .lessonId(request.getLessonId())
                .trackId(request.getTrackId())
                .title(request.getTitle())
                .build());
        return BookmarkResponse.from(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long userId, Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bookmark not found"));
        bookmarkRepository.delete(bookmark);
    }

    public List<QaResponse> lessonQa(Long lessonId) {
        return lessonQaRepository.findByLessonIdOrderByCreatedAtDesc(lessonId).stream()
                .map(QaResponse::from)
                .toList();
    }

    @Transactional
    public QaResponse postQuestion(Long userId, Long lessonId, QaRequest request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question is required");
        }
        LessonQa qa = lessonQaRepository.save(LessonQa.builder()
                .lessonId(lessonId)
                .userId(userId)
                .question(request.getQuestion())
                .build());
        return QaResponse.from(qa);
    }
}
