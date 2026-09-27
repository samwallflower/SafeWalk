package com.samwallflower.safewalk.service.email;

import java.time.Year;

public final class EmailTemplates {

    private EmailTemplates() {}

    // Shared design tokens — GitHub-light minimal
    private static final String BG = "#ffffff";
    private static final String CARD_BORDER = "#d0d7de";
    private static final String TEXT_PRIMARY = "#1f2328";
    private static final String TEXT_MUTED = "#59636e";
    private static final String ACCENT = "#0969da";
    private static final String DANGER = "#cf222e";
    private static final String DANGER_BG = "#fff1f0";
    private static final String SUCCESS = "#1a7f37";
    private static final String SUCCESS_BG = "#f0fff4";
    private static final String FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif";

    public static String verificationCode(String code) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background-color:#f6f8fa; font-family:%s;">
                  <table role="presentation" width="100%%" style="padding:40px 20px;">
                    <tr><td align="center">
                      <table role="presentation" width="100%%" style="max-width:480px; background-color:%s; border:1px solid %s; border-radius:12px; overflow:hidden;">
                        <tr>
                          <td style="padding:32px 32px 8px 32px;">
                            <h2 style="margin:0 0 4px 0; font-size:18px; color:%s;">Verify your email</h2>
                            <p style="margin:0; font-size:14px; color:%s;">SafeWalk</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:24px 32px;">
                            <p style="margin:0 0 20px 0; font-size:14px; line-height:1.6; color:%s;">
                              Enter this code to finish setting up your account. It expires in <strong>15 minutes</strong>.
                            </p>
                            <div style="background-color:#f6f8fa; border:1px solid %s; border-radius:8px; padding:20px; text-align:center;">
                              <span style="font-size:32px; font-weight:600; letter-spacing:8px; color:%s; font-family:'SFMono-Regular', Consolas, monospace;">%s</span>
                            </div>
                            <p style="margin:20px 0 0 0; font-size:13px; color:%s;">
                              If you didn't request this, you can safely ignore this email.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:20px 32px; border-top:1px solid %s;">
                            <p style="margin:0; font-size:12px; color:%s;">&copy; %d SafeWalk</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                FONT, BG, CARD_BORDER, TEXT_PRIMARY, TEXT_MUTED,
                TEXT_PRIMARY, CARD_BORDER, ACCENT, code, TEXT_MUTED,
                CARD_BORDER, TEXT_MUTED, Year.now().getValue()
        );
    }

    public static String emergencyAlert(String fullName, String trackingLink, String authorityLine) {
        String authorityBlock = authorityLine == null || authorityLine.isBlank()
                ? ""
                : """
                  <div style="background-color:%s; border:1px solid #ffd8d3; border-radius:8px; padding:14px 16px; margin:0 0 20px 0;">
                    <p style="margin:0; font-size:13px; color:%s;">%s</p>
                  </div>
                  """.formatted(DANGER_BG, TEXT_PRIMARY, authorityLine);

        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background-color:#f6f8fa; font-family:%s;">
                  <table role="presentation" width="100%%" style="padding:40px 20px;">
                    <tr><td align="center">
                      <table role="presentation" width="100%%" style="max-width:480px; background-color:%s; border:1px solid %s; border-radius:12px; overflow:hidden;">
                        <tr>
                          <td style="padding:20px 32px; background-color:%s; border-bottom:1px solid #ffd8d3;">
                            <span style="font-size:15px; font-weight:600; color:%s;">&#9888; Emergency Alert</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:28px 32px;">
                            <p style="margin:0 0 20px 0; font-size:14px; line-height:1.6; color:%s;">
                              <strong>%s</strong> may need help right now.
                            </p>
                            <table role="presentation" width="100%%"><tr><td align="center" style="padding:4px 0 24px 0;">
                              <a href="%s" style="display:inline-block; background-color:%s; color:#ffffff; font-size:14px; font-weight:600; text-decoration:none; padding:10px 20px; border-radius:6px;">
                                View live location
                              </a>
                            </td></tr></table>
                            %s
                            <p style="margin:0; font-size:12px; color:%s;">
                              Sent automatically by SafeWalk on behalf of %s.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:16px 32px; border-top:1px solid %s;">
                            <p style="margin:0; font-size:12px; color:%s;">&copy; %d SafeWalk</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                FONT, BG, CARD_BORDER, DANGER_BG, DANGER,
                TEXT_PRIMARY, fullName,
                trackingLink, DANGER,
                authorityBlock, TEXT_MUTED, fullName,
                CARD_BORDER, TEXT_MUTED, Year.now().getValue()
        );
    }

    public static String emergencyResolved(String fullName) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background-color:#f6f8fa; font-family:%s;">
                  <table role="presentation" width="100%%" style="padding:40px 20px;">
                    <tr><td align="center">
                      <table role="presentation" width="100%%" style="max-width:480px; background-color:%s; border:1px solid %s; border-radius:12px; overflow:hidden;">
                        <tr>
                          <td style="padding:20px 32px; background-color:%s; border-bottom:1px solid #aceebb;">
                            <span style="font-size:15px; font-weight:600; color:%s;">&#10003; Resolved</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:28px 32px;">
                            <p style="margin:0 0 8px 0; font-size:14px; line-height:1.6; color:%s;">
                              Good news — <strong>%s</strong> has confirmed they are safe.
                            </p>
                            <p style="margin:0 0 20px 0; font-size:14px; line-height:1.6; color:%s;">
                              The earlier emergency alert has been resolved. No further action is needed.
                            </p>
                            <p style="margin:0; font-size:12px; color:%s;">
                              Sent automatically by SafeWalk on behalf of %s.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:16px 32px; border-top:1px solid %s;">
                            <p style="margin:0; font-size:12px; color:%s;">&copy; %d SafeWalk</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                FONT, BG, CARD_BORDER, SUCCESS_BG, SUCCESS,
                TEXT_PRIMARY, fullName,
                TEXT_MUTED,
                TEXT_MUTED, fullName,
                CARD_BORDER, TEXT_MUTED, Year.now().getValue()
        );
    }
}