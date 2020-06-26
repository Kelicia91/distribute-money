package com.kakaopayhw.distributemoney.controller.support

import com.kakaopayhw.distributemoney.controller.interfaces.MessageKey
import com.kakaopayhw.distributemoney.controller.interfaces.RestExceptionView
import com.kakaopayhw.distributemoney.exception.DeniedAccessException
import com.kakaopayhw.distributemoney.exception.InvalidArgumentException
import com.kakaopayhw.distributemoney.exception.NotFoundEntityException
import com.kakaopayhw.distributemoney.service.MessageSourceService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestExceptionHandler(
    private val messageSourceService: MessageSourceService
) : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun handle(e: Exception, request: WebRequest): ResponseEntity<Any> {
        return handle(
            exception = e,
            request = request,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = messageSourceService.getMessage(MessageKey.EXCEPTION)
        )
    }

    @ExceptionHandler(InvalidArgumentException::class)
    fun handle(e: InvalidArgumentException, request: WebRequest): ResponseEntity<Any> {
        return handle(
            exception = e,
            request = request,
            status = HttpStatus.BAD_REQUEST,
            message = messageSourceService.getMessage(e.messageKey, e.objs)
        )
    }

    @ExceptionHandler(NotFoundEntityException::class)
    fun handle(e: NotFoundEntityException, request: WebRequest): ResponseEntity<Any> {
        return handle(
            exception = e,
            request = request,
            status = HttpStatus.BAD_REQUEST,
            message = messageSourceService.getMessage(e.messageKey, e.objs)
        )
    }

    @ExceptionHandler(DeniedAccessException::class)
    fun handle(e: DeniedAccessException, request: WebRequest): ResponseEntity<Any> {
        return handle(
            exception = e,
            request = request,
            status = HttpStatus.FORBIDDEN,
            message = messageSourceService.getMessage(e.messageKey, e.objs)
        )
    }

    private fun handle(
        exception: Exception,
        request: WebRequest,
        header: HttpHeaders = HttpHeaders.EMPTY,
        status: HttpStatus,
        message: String
    ): ResponseEntity<Any> {
        return handleExceptionInternal(
            exception,
            RestExceptionView(message),
            header,
            status,
            request
        )
    }
}