type AnyFn = (...args: never[]) => unknown;

type MethodKeys<T> = {
  [K in keyof T]-?: T[K] extends AnyFn ? K : never;
}[keyof T];

export type SpyObj<
  T extends object,
  M extends MethodKeys<T> = never,
> = Partial<T> & Record<M, ReturnType<typeof vi.fn>>;

export function createSpyObj<
  T extends object,
  M extends MethodKeys<T> = MethodKeys<T>,
>(methods: M[], properties: Partial<T> = {}): SpyObj<T, M> {
  const spy = {} as SpyObj<T, M>;

  for (const method of methods) {
    (spy as Record<string, unknown>)[method as string] = vi.fn();
  }

  Object.assign(spy, properties);
  return spy;
}
