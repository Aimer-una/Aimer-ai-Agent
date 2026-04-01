package com.aimer.agent.advisor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor {
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

//    @Override
//    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
//        advisedRequest = this.before(advisedRequest);
//        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
//        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses,this::observeAfter);
//    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private AdvisedRequest before(AdvisedRequest request){
        log.info("AI Request: {}", request.userText());
        return request;
    }

    private void observeAfter(AdvisedResponse advisedResponse){
        log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
    }
}
