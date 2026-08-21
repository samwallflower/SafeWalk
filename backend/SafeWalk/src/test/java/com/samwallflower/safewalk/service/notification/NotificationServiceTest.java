package com.samwallflower.safewalk.service.notification;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.AlertMessageType;
import com.samwallflower.safewalk.websocket.AlertMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    void pushLocationUpdate_sendsToCorrectSessionTopic() {
        NotificationService service = new NotificationService(simpMessagingTemplate);
        WalkSessionDto dto = new WalkSessionDto();
        dto.setId(4L);

        service.pushLocationUpdate(4L, dto);

        verify(simpMessagingTemplate).convertAndSend("/topic/session/4", dto);
    }

    @Test
    void pushEmergencyAlert_sendsToCorrectAlertTopic() {
        NotificationService service = new NotificationService(simpMessagingTemplate);
        AlertMessage alert = new AlertMessage(4L, AlertMessageType.IDLE_WARNING, "test message");

        service.pushEmergencyAlert(4L, alert);

        verify(simpMessagingTemplate).convertAndSend("/topic/alert/4", alert);
    }

    @Test
    void pushIdleWarning_buildsCorrectAlertMessage() {
        NotificationService service = new NotificationService(simpMessagingTemplate);

        service.pushIdleWarning(4L);

        ArgumentCaptor<AlertMessage> captor = ArgumentCaptor.forClass(AlertMessage.class);
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/alert/4"), captor.capture());

        AlertMessage sent = captor.getValue();
        assertThat(sent.getSessionId()).isEqualTo(4L);
        assertThat(sent.getType()).isEqualTo(AlertMessageType.IDLE_WARNING);
        assertThat(sent.getMessage()).contains("No movement detected");
    }

    @Test
    void pushRouteDeviationWarning_buildsCorrectAlertMessage() {
        NotificationService service = new NotificationService(simpMessagingTemplate);

        service.pushRouteDeviationWarning(4L);

        ArgumentCaptor<AlertMessage> captor = ArgumentCaptor.forClass(AlertMessage.class);
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/alert/4"), captor.capture());

        AlertMessage sent = captor.getValue();
        assertThat(sent.getSessionId()).isEqualTo(4L);
        assertThat(sent.getType()).isEqualTo(AlertMessageType.ROUTE_DEVIATION);
        assertThat(sent.getMessage()).contains("deviated from your planned route");
    }

    @Test
    void pushIdleWarning_andPushRouteDeviationWarning_useSameTopicPrefix_asPushEmergencyAlert() {
        NotificationService service = new NotificationService(simpMessagingTemplate);

        service.pushIdleWarning(5L);
        service.pushRouteDeviationWarning(5L);
        service.pushEmergencyAlert(5L, new AlertMessage(5L, AlertMessageType.IDLE_WARNING, "manual"));

        verify(simpMessagingTemplate, times(3))
                .convertAndSend(eq("/topic/alert/5"), any(AlertMessage.class));
    }
}