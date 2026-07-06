package com.sportbuddy.service;

import com.sportbuddy.entity.*;
import com.sportbuddy.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CourtService} — booking / ranked-match / cancellation
 * rules with every repository mocked.
 */
@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock private RankedMatchRepository rankedMatchRepository;
    @Mock private SportTypeRepository sportTypeRepository;
    @Mock private SportCourtRepository sportCourtRepository;
    @Mock private CourtTimeSlotRepository courtTimeSlotRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CityRepository cityRepository;

    @InjectMocks
    private CourtService courtService;

    private User user(long id) {
        User u = new User("A", "B", "a@b.com", "+70000000000", "hash");
        u.setId(id);
        return u;
    }

    private CourtTimeSlot slot(LocalDateTime start, LocalDateTime end) {
        return new CourtTimeSlot(null, start, end, 0);
    }

    // ---- bookTimeSlot ----

    @Test
    void bookTimeSlot_success_savesBooking() {
        User u = user(1L);
        CourtTimeSlot s = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        when(courtTimeSlotRepository.findById(10L)).thenReturn(Optional.of(s));
        when(bookingRepository.existsByUserIdAndCourtTimeSlotId(1L, 10L)).thenReturn(false);
        when(bookingRepository.existsOverlappingBooking(eq(1L), any(), any())).thenReturn(false);

        boolean result = courtService.bookTimeSlot(u, 10L);

        assertThat(result).isTrue();
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookTimeSlot_slotNotFound_returnsFalse() {
        User u = user(1L);
        when(courtTimeSlotRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = courtService.bookTimeSlot(u, 99L);

        assertThat(result).isFalse();
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTimeSlot_alreadyBookedSameSlot_returnsFalse() {
        User u = user(1L);
        CourtTimeSlot s = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        when(courtTimeSlotRepository.findById(10L)).thenReturn(Optional.of(s));
        when(bookingRepository.existsByUserIdAndCourtTimeSlotId(1L, 10L)).thenReturn(true);

        boolean result = courtService.bookTimeSlot(u, 10L);

        assertThat(result).isFalse();
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTimeSlot_overlappingBookingElsewhere_returnsFalse() {
        User u = user(1L);
        CourtTimeSlot s = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        when(courtTimeSlotRepository.findById(10L)).thenReturn(Optional.of(s));
        when(bookingRepository.existsByUserIdAndCourtTimeSlotId(1L, 10L)).thenReturn(false);
        when(bookingRepository.existsOverlappingBooking(eq(1L), any(), any())).thenReturn(true);

        boolean result = courtService.bookTimeSlot(u, 10L);

        assertThat(result).isFalse();
        verify(bookingRepository, never()).save(any());
    }

    // ---- joinRankedMatch ----

    @Test
    void joinRankedMatch_success_addsParticipant() {
        User u = user(5L);
        RankedMatch match = new RankedMatch();
        when(rankedMatchRepository.findById(3L)).thenReturn(Optional.of(match));

        boolean result = courtService.joinRankedMatch(3L, u);

        assertThat(result).isTrue();
        assertThat(match.getParticipants()).contains(u);
        verify(rankedMatchRepository).save(match);
    }

    @Test
    void joinRankedMatch_matchNotFound_throws() {
        User u = user(5L);
        when(rankedMatchRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtService.joinRankedMatch(404L, u))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Матч не найден");
    }

    @Test
    void joinRankedMatch_sameUserTwice_isIdempotent() {
        // There is no participant cap in the domain; the Set dedups the user.
        User u = user(5L);
        RankedMatch match = new RankedMatch();
        when(rankedMatchRepository.findById(3L)).thenReturn(Optional.of(match));

        courtService.joinRankedMatch(3L, u);
        courtService.joinRankedMatch(3L, u);

        assertThat(match.getParticipants()).hasSize(1);
    }

    // ---- cancelBooking ----

    @Test
    void cancelBooking_ownerCancels_returnsTrueAndDeletes() {
        User owner = user(7L);
        Booking booking = new Booking(owner, slot(LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
        when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));

        boolean result = courtService.cancelBooking(20L, 7L);

        assertThat(result).isTrue();
        verify(bookingRepository).delete(booking);
    }

    @Test
    void cancelBooking_notOwner_returnsFalse() {
        User owner = user(7L);
        Booking booking = new Booking(owner, slot(LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
        when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));

        boolean result = courtService.cancelBooking(20L, 999L);

        assertThat(result).isFalse();
        verify(bookingRepository, never()).delete(any());
    }

    @Test
    void cancelBooking_notFound_returnsFalse() {
        when(bookingRepository.findById(20L)).thenReturn(Optional.empty());

        boolean result = courtService.cancelBooking(20L, 7L);

        assertThat(result).isFalse();
    }

    // ---- approve / reject court ----

    @Test
    void approveCourt_setsApprovedStatusAndSaves() {
        SportCourt court = new SportCourt();
        court.setName("Корт №1");
        court.setStatus("PENDING_APPROVAL");
        when(sportCourtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(courtTimeSlotRepository.findByCourtId(any())).thenReturn(List.of());

        courtService.approveCourt(1L);

        assertThat(court.getStatus()).isEqualTo("APPROVED");
        verify(sportCourtRepository).save(court);
    }

    @Test
    void rejectCourt_deletesCourt() {
        SportCourt court = new SportCourt();
        when(sportCourtRepository.findById(1L)).thenReturn(Optional.of(court));

        courtService.rejectCourt(1L);

        verify(sportCourtRepository).delete(court);
    }

    // ---- getTimeSlotsByCourt ----

    @Test
    void getTimeSlotsByCourt_returnsOnlyFutureSlots() {
        CourtTimeSlot past = slot(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1));
        CourtTimeSlot future = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        when(courtTimeSlotRepository.findByCourtId(1L)).thenReturn(List.of(past, future));

        List<CourtTimeSlot> result = courtService.getTimeSlotsByCourt(1L);

        assertThat(result).containsExactly(future);
    }

    // ---- createCustomCourt ----

    @Test
    void createCustomCourt_setsOwnerPendingStatusAndResolvesSportType() {
        User owner = user(3L);
        SportType type = new SportType();
        type.setId(2L);
        SportCourt input = new SportCourt();
        input.setName("Новый корт");
        input.setSportType(type);
        when(sportTypeRepository.findById(2L)).thenReturn(Optional.of(type));
        when(sportCourtRepository.save(any(SportCourt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(courtTimeSlotRepository.findByCourtId(any())).thenReturn(List.of());

        SportCourt result = courtService.createCustomCourt(input, owner);

        assertThat(result.getStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(result.getOwner()).isEqualTo(owner);
        verify(sportCourtRepository).save(input);
    }

    // ---- lockChatCreation ----

    @Test
    void lockChatCreation_whenSlotFree_locksAndReturnsTrue() {
        User u = user(1L);
        CourtTimeSlot s = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        when(courtTimeSlotRepository.findById(10L)).thenReturn(Optional.of(s));

        boolean result = courtService.lockChatCreation(10L, u);

        assertThat(result).isTrue();
        assertThat(s.getChatCreator()).isEqualTo(u);
        assertThat(s.getChatCreationLockTime()).isAfter(LocalDateTime.now());
        verify(courtTimeSlotRepository).save(s);
    }

    @Test
    void lockChatCreation_whenAlreadyLocked_returnsFalse() {
        CourtTimeSlot s = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        s.setChatCreationLockTime(LocalDateTime.now().plusMinutes(5));
        when(courtTimeSlotRepository.findById(10L)).thenReturn(Optional.of(s));

        boolean result = courtService.lockChatCreation(10L, user(1L));

        assertThat(result).isFalse();
        verify(courtTimeSlotRepository, never()).save(any());
    }

    // ---- saveChatUrl ----

    @Test
    void saveChatUrl_whenCreatorAndLocked_savesUrlAndClearsLock() {
        User u = user(1L);
        CourtTimeSlot s = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        s.setChatCreator(u);
        s.setChatCreationLockTime(LocalDateTime.now().plusMinutes(5));
        when(courtTimeSlotRepository.findById(10L)).thenReturn(Optional.of(s));

        boolean result = courtService.saveChatUrl(10L, "https://t.me/game", u);

        assertThat(result).isTrue();
        assertThat(s.getChatUrl()).isEqualTo("https://t.me/game");
        assertThat(s.getChatCreator()).isNull();
        assertThat(s.getChatCreationLockTime()).isNull();
    }

    @Test
    void saveChatUrl_whenNotCreator_returnsFalse() {
        User creator = user(1L);
        User other = user(2L);
        CourtTimeSlot s = slot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        s.setChatCreator(creator);
        s.setChatCreationLockTime(LocalDateTime.now().plusMinutes(5));
        when(courtTimeSlotRepository.findById(10L)).thenReturn(Optional.of(s));

        boolean result = courtService.saveChatUrl(10L, "https://t.me/game", other);

        assertThat(result).isFalse();
    }

    // ---- addReview / delegations ----

    @Test
    void addReview_persistsReviewForCourt() {
        SportCourt court = new SportCourt();
        when(sportCourtRepository.findById(1L)).thenReturn(Optional.of(court));

        courtService.addReview(1L, user(1L), 5, "Отличный корт");

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void getBookingCountForTimeSlot_delegatesToRepository() {
        when(bookingRepository.countByCourtTimeSlotId(10L)).thenReturn(3);

        assertThat(courtService.getBookingCountForTimeSlot(10L)).isEqualTo(3);
    }

    @Test
    void createRankedMatch_savesMatchWithGivenFields() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        courtService.createRankedMatch("Турнир", "Парк", "ул. Ленина", "desc", start, end);

        verify(rankedMatchRepository).save(any(RankedMatch.class));
    }

    @Test
    void getParticipantsForTimeSlot_mapsBookingsToParticipants() {
        User u = user(1L);
        Booking booking = new Booking(u, slot(LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
        when(bookingRepository.findByCourtTimeSlotId(10L)).thenReturn(List.of(booking));

        var participants = courtService.getParticipantsForTimeSlot(10L);

        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getFirstName()).isEqualTo("A");
    }

    @Test
    void deleteCourt_removesSlotsBookingsReviewsAndCourt() {
        SportCourt court = new SportCourt();
        CourtTimeSlot s = slot(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        when(sportCourtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(courtTimeSlotRepository.findByCourtId(1L)).thenReturn(List.of(s));
        when(bookingRepository.findByCourtTimeSlotId(any())).thenReturn(List.of());
        when(reviewRepository.findByCourtId(1L)).thenReturn(List.of());

        courtService.deleteCourt(1L);

        verify(courtTimeSlotRepository).deleteAll(any());
        verify(sportCourtRepository).delete(court);
    }
}
