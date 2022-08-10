import { RefObject, useEffect, useState } from 'react';
import { EMPTY, fromEvent, map, Observable } from 'rxjs';

const useMouseMove = <T extends HTMLElement>(
  target: RefObject<T>,
): Observable<{ x: number; y: number }> => {
  const [event$, setEvent$] =
    useState<Observable<{ x: number; y: number }>>(EMPTY);

  useEffect(() => {
    if (target?.current) {
      const event$ = fromEvent<MouseEvent>(target?.current, 'mousemove').pipe(
        map((event) => ({ x: event.clientX, y: event.clientY })),
      );

      setEvent$(event$);
    } else {
      setEvent$(EMPTY);
    }
  }, [target]);

  return event$;
};

export default useMouseMove;
