import { BehaviorSubject, Observable, Subject, Subscription } from 'rxjs';

export default class NodeContext {
  readonly radious$: Observable<number>;
  readonly boundry: { l: number; r: number; t: number; b: number };
  readonly position$: Observable<{ x: number; y: number }>;
  private readonly position: BehaviorSubject<{ x: number; y: number }>;
  private currentPosition: { x: number; y: number };
  private readonly mousePosition$: Observable<{ x: number; y: number }>;
  private mouseSuscription: Subscription = Subscription.EMPTY;

  constructor(
    boundry: { l: number; r: number; t: number; b: number },
    mousePosition$: Observable<{ x: number; y: number }>,
    position: { x: number; y: number },
    radious$: Observable<number>,
  ) {
    this.boundry = boundry;
    this.mousePosition$ = mousePosition$;
    this.radious$ = radious$;
    this.currentPosition = position;
    this.position = new BehaviorSubject(position);
    this.position$ = this.position.asObservable();
  }
  changePosition(x: number, y: number) {
    this.currentPosition = { x, y };
    this.position.next(this.currentPosition);
    console.log(this.currentPosition);
  }

  onMouseUp() {
    this.mouseSuscription?.unsubscribe();
    console.log('mouseUp');
  }

  onMouseDown({ x, y }: { x: number; y: number }) {
    this.mouseSuscription = this.mousePosition$.subscribe(
      ({ x: mouseX, y: mouseY }) => {
        const posX = mouseX - (x - this.currentPosition.x);
        const posY = mouseY - (y - this.currentPosition.y);

        this.changePosition(posX, posY);
      },
    );
    console.log('mouseDown');
  }
}
