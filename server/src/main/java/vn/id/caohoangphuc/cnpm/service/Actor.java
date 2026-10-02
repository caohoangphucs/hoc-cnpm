package vn.id.caohoangphuc.cnpm.service;

/**
 * Created from the authenticated session; callers must never take actor IDs from request JSON.
 */
public record Actor(int accountId, boolean twoFactorVerified) {}
