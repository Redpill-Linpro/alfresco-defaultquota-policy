# Alfresco Default Quota Policy

- Author: Niklas Ekman (niklas.ekman@redpill-linpro.com) & Jared Ottley (jared.ottley@alfresco.com)
- Create date: 2014-05-25
- Version: 1.0.0

## Summary
This extension adds the ability to define and apply a default quota when a new user is created.

Alfresco's default quota is unlimited. This extension sets the default to 2 GB, unless a value is provided at user creation time, which then overrides the default.

The default value is set in a context file and must be defined in bytes.

Currently tested with 4.2.2

Bases on the original code by Jared Ottley, Mavenized and updated for Alfresco Enterprise 4.2.2.
