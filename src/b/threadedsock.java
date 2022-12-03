package b;

/**
 * Marker interface for sock to be run on own thread instead of the server thread. If the sock is doing any significant work it should run on own thread instead of blocking server. If the sock needs access to the req it must run on own thread and session_id cookie being set.
 */
public interface threadedsock{
}
