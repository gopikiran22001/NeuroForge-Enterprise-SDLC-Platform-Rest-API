package com.stdace.neuroforge.enums;

public enum UserRole {
    SUPER_ADMIN,       // Platform-level: manages organizations globally
    ORG_ADMIN,         // Organization-level: manages their own organization's data
    PROJECT_MANAGER,
    DEVELOPER,
    TESTER,
    DEVOPS_ENGINEER,
}
