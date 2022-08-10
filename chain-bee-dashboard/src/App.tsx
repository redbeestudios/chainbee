import { useEffect, useMemo, useRef } from 'react';
import { BehaviorSubject, EMPTY, fromEvent, map } from 'rxjs';
import Node from './node/node';
import NodeContext from './node/node-context';

function App() {
  const containerRef = useRef<HTMLDivElement>(null);

  const radious = useMemo(() => {
    return new BehaviorSubject(25);
  }, []);

  const mousePosition$ = useMemo(() => {
    return containerRef?.current
      ? fromEvent<MouseEvent>(containerRef.current, 'mousemove').pipe(
          map((event) => {
            console.log(event);
            return { x: event.clientX, y: event.clientY };
          }),
        )
      : EMPTY;
  }, [containerRef]);

  const node = useMemo(() => {
    return new NodeContext(
      { l: 200, r: 700, t: 200, b: 700 },
      mousePosition$,
      { x: 400, y: 400 },
      radious.asObservable(),
    );
  }, [mousePosition$, radious]);

  useEffect(() => {
    mousePosition$.subscribe((position) => {
      console.log(position);
    });
  }, [mousePosition$]);

  return (
    <div ref={containerRef} className="w-screen min-h-screen">
      <Node name="Test Node" node={node} />
    </div>
  );
}

export default App;
