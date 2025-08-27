package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.handler.RecommendationHandler;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationHandler recommendationHandler;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto requestProto,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("request for user recommendation {}", requestProto);
            List<RecommendedEventProto> recommendations = recommendationHandler.handle(requestProto);
            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvent(SimilarEventRequestProto requestProto,
                                StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("request for similar events {}", requestProto);
            List<RecommendedEventProto> recommendations = recommendationHandler.handle(requestProto);
            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto requestProto,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("request for interactions count {}", requestProto);
            List<RecommendedEventProto> recommendations = recommendationHandler.handle(requestProto);
            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

}
