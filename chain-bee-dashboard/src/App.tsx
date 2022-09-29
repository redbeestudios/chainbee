import { useMemo, useRef } from 'react';
import { BehaviorSubject } from 'rxjs';
import useMouseMove from './hooks/use-mouse-move';
import Node from './node/node';
import NodeContext from './node/node-context';

function App() {
  const containerRef = useRef<HTMLDivElement>(null);
  const mousePosition$ = useMouseMove(containerRef);

  const radius = useMemo(() => {
    return new BehaviorSubject(100);
  }, []);

  const node1 = useMemo(() => {
    return new NodeContext(
      { l: 200, r: 700, t: 200, b: 700 },
      mousePosition$,
      { x: 400, y: 400 },
      radius.asObservable(),
    );
  }, [mousePosition$, radius]);

  const node2 = useMemo(() => {
    return new NodeContext(
      { l: 200, r: 700, t: 200, b: 700 },
      mousePosition$,
      { x: 200, y: -110 },
      radius.asObservable(),
    );
  }, [mousePosition$, radius]);

  const node3 = useMemo(() => {
    return new NodeContext(
      { l: 50, r: 50, t: 50, b: 50 },
      mousePosition$,
      { x: 600, y: -310 },
      radius.asObservable(),
    );
  }, [mousePosition$, radius]);

  return (
    <div
      ref={containerRef}
      className="w-screen min-h-screen bg-[url('src/assets/bg-main.jpg')] bg-cover"
    >
      <Node name="Test Node 1" node={node1} />
      <Node name="Test Node 2" node={node2} />
      <Node name="Test Node 3" node={node3} />
    </div>
  );
}

export default App;
