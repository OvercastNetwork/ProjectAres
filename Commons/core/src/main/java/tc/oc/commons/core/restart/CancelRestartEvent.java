package tc.oc.commons.core.restart;

/**
 * Fired by {@link RestartManager} when a requested restart is cancelled.
 * Any registered {@link RequestRestartEvent.Deferral}s are forgotten by
 * the manager and can be discarded.
 */
public class CancelRestartEvent {}
