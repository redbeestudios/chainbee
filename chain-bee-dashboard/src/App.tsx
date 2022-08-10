import { useEffect, useMemo, useRef } from 'react';
import { BehaviorSubject, EMPTY, fromEvent, map } from 'rxjs';
import useMouseMove from './hooks/use-mouse-move';
import Node from './node/node';
import NodeContext from './node/node-context';

function App() {
  const containerRef = useRef<HTMLDivElement>(null);
  const mousePosition$ = useMouseMove(containerRef);

  const radius = useMemo(() => {
    return new BehaviorSubject(100);
  }, []);

  const node = useMemo(() => {
    return new NodeContext(
      { l: 200, r: 700, t: 200, b: 700 },
      mousePosition$,
      { x: 400, y: 400 },
      radius.asObservable(),
    );
  }, [mousePosition$, radius]);

  return (
    <div ref={containerRef} className="w-screen min-h-screen">
      <Node name="Test Node" node={node} />
    </div>
  );
}

export default App;
