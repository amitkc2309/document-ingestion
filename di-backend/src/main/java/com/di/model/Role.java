package com.di.model;

public enum Role {
    ADMIN,    // Can manage users and all documents
    EDITOR,   // Can upload and edit documents
    VIEWER    // Can only view documents
}