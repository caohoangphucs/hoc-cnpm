package vn.id.caohoangphuc.cnpm.service;

/**
 * Created by the future authentication layer from its verified session, never from request JSON.
 */
public record Actor(int accountId, boolean twoFactorVerified) {}
