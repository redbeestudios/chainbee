# Chainbee Dashboard

A web app to explore the Chainbee blockchain

## Develop

### Installing

1. Set up node version `nvm use`
2. Install yarn `npm install -g yarn`
3. Install deps `yarn install`

### Running

Run the project in development mode with `yarn dev`

### Code conventions

#### Linter / Formatter

We are using Prettier (as formatter) alongside ESLint (as linter). Make sure your IDE / Text Editor supports both.

#### Folder structure (TBD)

#### Styles

Prefer using [Tailwind](https://tailwindcss.com/) over CSS. If Tailwind it's not enough, create a CSS Module and import it on the specific component that needs those styles.

#### Props typings

Always type component props with a custom type. This will help anyone who wants to use that component to know what it can do and what it requires to work.

```typescript
type TimerProps = {
  startTime: number;
  onFinish: () => void;
};

const Timer = ({ startTime, onFinish }: TimerProps) => {
  // [...]
};
```

## Packaging (WIP)

We are still working no packaging. The idea is to have an npm script alongside a Dockerfile.
