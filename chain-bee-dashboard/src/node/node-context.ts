import { BehaviorSubject, Observable, Subscription } from 'rxjs';

type Boundary = { l: number; r: number; t: number; b: number };
type Position = { x: number; y: number };
export default class NodeContext {
  readonly radius$: Observable<number>;
  readonly boundary: Boundary;
  readonly position$: Observable<Position>;
  private readonly position: BehaviorSubject<Position>;
  private currentPosition: Position;
  private readonly mousePosition$: Observable<Position>;
  private mouseSubscription: Subscription = Subscription.EMPTY;

  constructor(
    boundary: Boundary,
    mousePosition$: Observable<Position>,
    position: Position,
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

  onMouseDown({ x: clickedX, y: clickedY }: Position) {
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
