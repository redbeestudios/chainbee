import { BehaviorSubject, Observable, Subscription } from 'rxjs';

export default class NodeContext {
  readonly radius$: Observable<number>;
  readonly boundary: { l: number; r: number; t: number; b: number };
  readonly position$: Observable<{ x: number; y: number }>;
  private readonly position: BehaviorSubject<{ x: number; y: number }>;
  private currentPosition: { x: number; y: number };
  private readonly mousePosition$: Observable<{ x: number; y: number }>;
  private mouseSubscription: Subscription = Subscription.EMPTY;

  constructor(
    boundary: { l: number; r: number; t: number; b: number },
    mousePosition$: Observable<{ x: number; y: number }>,
    position: { x: number; y: number },
    radius$: Observable<number>,
  ) {
    this.boundary = boundary;
    this.mousePosition$ = mousePosition$;
    this.radius$ = radius$;
    this.currentPosition = position;
    this.position = new BehaviorSubject(position);
    this.position$ = this.position.asObservable();
  }
  changePosition(x: number, y: number) {
    this.currentPosition = { x, y };
    this.position.next(this.currentPosition);
  }

  onMouseUp() {
    this.mouseSubscription?.unsubscribe();
  }

  onMouseDown({ x: clickedX, y: clickedY }: { x: number; y: number }) {
    const currentX = this.currentPosition.x;
    const currentY = this.currentPosition.y;

    this.mouseSubscription = this.mousePosition$.subscribe(
      ({ x: mouseX, y: mouseY }) => {
        const posX = Math.min(
          Math.max(mouseX - (clickedX - currentX), this.boundary.l),
          this.boundary.r,
        );
        const posY = Math.min(
          Math.max(mouseY - (clickedY - currentY), this.boundary.t),
          this.boundary.b,
        );

        this.changePosition(posX, posY);
      },
    );
  }
}
