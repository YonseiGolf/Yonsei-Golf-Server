package yonseigolf.server.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yonseigolf.server.board.exception.BoardNotFoundException;
import yonseigolf.server.board.exception.DeletedBoardException;
import yonseigolf.server.util.CustomErrorResponse;

@RestControllerAdvice
public class BoardExceptionController {

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity boardNotFound(BoardNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse(
                        "not found",
                        404,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(DeletedBoardException.class)
    public ResponseEntity deletedBoard(DeletedBoardException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse(
                        "not found",
                        404,
                        ex.getMessage()
                ));
    }
}
