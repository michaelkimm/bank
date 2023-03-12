package com.ms.bank.transfer.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.transfer.application.ExternalDepositService;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferScheduler {

    private final ExternalDepositService externalDepositService;
    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;

    private WebClient webClient = WebClient.create("http://localhost:8080");
    private final ObjectMapper objectMapper;

    @Transactional
    @Async("transferSchedulerAsyncExecutor")
    @Scheduled(fixedDelay = 100)
    public void processTransferOutBoxMessage() {

        Optional<ExternalTransferOutBox> outbox = externalTransferOutBoxRepository.findOneForUpdate();
        if (outbox.isEmpty()) {
            return;
        }
        ExternalTransferOutBox outBoxForUpdate = outbox.get();

        TransferHistory transferHistory = getTransferHistory(outBoxForUpdate);

        // 입금 요청
        ExternalDepositRequestDto externalDepositRequestDto = ExternalDepositRequestDto.of(transferHistory);
        String body = null;
        try {
            body = objectMapper.writeValueAsString(externalDepositRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("parsing error");
        }
        Mono<ClientResponse> responseMono = webClient.post()
                .uri("/account/transfer/deposit/post")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                        .exchangeToMono(response -> Mono.just(response));

        HttpStatus status = responseMono.block().statusCode();
        if (!status.is2xxSuccessful()) {
            return;
        }

        // 아웃 박스 삭제
        externalTransferOutBoxRepository.delete(outBoxForUpdate);
    }

    @Transactional
    @Async("transferSchedulerAsyncExecutor")
    @Scheduled(fixedDelay = 100)
    public void processTransferDepositOutBoxMessage() {

        Optional<ExternalTransferDepositOutBox> outbox = externalTransferDepositOutBoxRepository.findOneForUpdate();
        if (outbox.isEmpty()) {
            return;
        }
        ExternalTransferDepositOutBox outBoxForUpdate = outbox.get();

        ExternalDepositRequestDto externalDepositRequestDto = getExternalDepositRequestDto(outBoxForUpdate);

        // 입금 처리
        boolean result = externalDepositService.execute(externalDepositRequestDto);
        if (!result) {
            return;
        }

        // 아웃 박스 삭제
        externalTransferDepositOutBoxRepository.delete(outBoxForUpdate);
    }

    private TransferHistory getTransferHistory(ExternalTransferOutBox outBoxForUpdate) {
        TransferHistory transferHistory = null;
        try {
            transferHistory = objectMapper.readValue(outBoxForUpdate.getPayLoad(), TransferHistory.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        return transferHistory;
    }

    private ExternalDepositRequestDto getExternalDepositRequestDto(ExternalTransferDepositOutBox outBoxForUpdate) {
        ExternalDepositRequestDto externalDepositRequestDto = null;
        try {
            externalDepositRequestDto = objectMapper.readValue(outBoxForUpdate.getPayLoad(), ExternalDepositRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        return externalDepositRequestDto;
    }
}
