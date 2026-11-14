# Contributing to FlowGuard

Thank you for considering contributing to FlowGuard!

## How to contribute

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes
4. Run tests: `mvn test` in the relevant service directory
5. Commit: `git commit -m "feat: describe your change"`
6. Push: `git push origin feature/your-feature-name`
7. Open a Pull Request against `main`

## Reporting issues

Use GitHub Issues. Include:
- Steps to reproduce
- Expected behaviour
- Actual behaviour
- Logs from `docker compose logs <service-name>`

## Development setup

```bash
cp .env.example .env
docker compose up postgres rabbitmq   # start dependencies only
# Then run individual services from your IDE
```

## Code style

- Java services: standard Spring Boot conventions, Lombok for boilerplate
- React: functional components, React Query for data fetching
- No commented-out code in PRs

## License

By contributing, you agree your contributions are licensed under the MIT License.
