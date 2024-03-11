package ru.practicum.ewm.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.enums.ParticipationState;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "requests")
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @JoinColumn(name = "requester_id")
    @ManyToOne
    private User requester;
    @Column(name = "created")
    private LocalDateTime created;
    @JoinColumn(name = "event_id")
    @ManyToOne
    private Event event;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ParticipationState status;
}
