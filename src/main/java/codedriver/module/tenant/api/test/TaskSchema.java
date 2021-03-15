package codedriver.module.tenant.api.test;

class TaskSchema {
    /*@Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TaskAttribute {
        String value();
        boolean ignore() default false;
    }

    enum TaskStatus {
        CREATED(0),
        PROCESSING(1),
        SUSPENDED(2),
        CLOSED(3),
        CANCELLED(4);

        private int code;

        TaskStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TaskStatus of(int code) {
            TaskStatus[] statuses = TaskStatus.values();
            for (TaskStatus t : statuses) {
                if (t.code == code) {
                    return t;
                }
            }
            return null;
        }
    }

    interface AttributeSetter {
        void set(MultiAttrsObjectPatch patch, String key, JSONObject json);
    }

    interface AttributeGetter {
        boolean get(Object dst, String attr, MultiAttrsObject src);
    }

    @TaskAttribute(value = "", ignore = true)
    private String id;
    private String title;
    private String name;
    @TaskAttribute("created_at")
    private Date createdAt;
    private List<String> tags;
    private TaskStatus status = TaskStatus.CREATED;
    private String handler;

    private static final Map<String, AttributeSetter> TASK_SETTERS = new HashMap<>();
    private static final Map<String, AttributeGetter> TASK_GETTERS = new HashMap<>();

    static {
        Field[] fields = TaskSchema.class.getDeclaredFields();
        for (Field f : fields) {
            int modifier = f.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier)) {
                continue;
            }
            TaskAttribute attr = f.getAnnotation(TaskAttribute.class);
            if (attr != null && attr.ignore()) {
                continue;
            }
            String attrName = attr == null ? f.getName() : attr.value();
            AttributeSetter setter = setterOf(f);
            if (setter != null) {
                TASK_SETTERS.put(attrName, setter);
            }
            AttributeGetter getter = getterOf(f);
            if (getter != null) {
                f.setAccessible(true);
                TASK_GETTERS.put(attrName, getter);
            }
        }
    }

    private static AttributeSetter setterOf(Field field) {
        Class<?> clazz = field.getType();
        if (clazz == String.class) {
            return stringSetter();
        } else if (clazz == Integer.class || clazz == int.class) {
            return integerSetter();
        } else if (clazz == Double.class || clazz == double.class) {
            return doubleSetter();
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return booleanSetter();
        } else if (clazz == Date.class) {
            return dateSetter();
        } else if (clazz == TaskStatus.class) {
            return (patch, key, json) -> {
                Object value = json.get(key);
                if (value == null) {
                    return;
                }
                TaskStatus status = null;
                if (value instanceof String) {
                    try {
                        status = TaskStatus.valueOf((String) value);
                    } catch (IllegalArgumentException ignore) {
                        try {
                            status = TaskStatus.of(Integer.parseInt((String)value));
                        } catch (NumberFormatException ignore2) {
                        }
                    }
                } else if (value instanceof Integer) {
                    status = TaskStatus.of((Integer) value);
                }
                if (status != null) {
                    patch.set(key, status.getCode());
                }
            };
        } else if (clazz == List.class) {
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            Class<?> elementType = (Class<?>) type.getActualTypeArguments()[0];
            if (elementType == String.class) {
                return stringListSetter();
            } else if (elementType == Integer.class || elementType == int.class) {
                return integerListSetter();
            }
        } else if (clazz == String[].class) {
            return stringListSetter();
        } else if (clazz == int[].class || clazz == Integer[].class) {
            return integerListSetter();
        }
        return null;
    }

    private static AttributeSetter stringSetter() {
        return (patch, key, json) -> {
            Object value = json.get(key);
            if (value == null) {
                return;
            }
            String v = null;
            if (value instanceof String) {
                v = (String) value;
            } else if (value instanceof Number) {
                v = value.toString();
            }
            if (v != null) {
                patch.set(key, v);
            }
        };
    }

    private static AttributeSetter doubleSetter() {
        return (patch, key, json) -> {
            Object value = json.get(key);
            if (value == null) {
                return;
            }
            Double v = null;
            if (value instanceof Double) {
                v = (Double) value;
            } else if (value instanceof Float) {
                v = ((Float) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    v = Double.parseDouble((String) value);
                } catch (NumberFormatException ignore) {
                }
            }
            if (v != null) {
                patch.set(key, v);
            }
        };
    }

    private static AttributeSetter dateSetter() {
        return (patch, key, json) -> {
            Object value = json.get(key);
            if (value == null) {
                return;
            }
            try {
                Date v = json.getDate(key);
                if (v != null) {
                    patch.set(key, v, true);
                }
            } catch (JSONException ignore) {
            }
        };
    }

    private static AttributeSetter booleanSetter() {
        return (patch, key, json) -> {
            Object value = json.get(key);
            if (value == null) {
                return;
            }
            Boolean v = null;
            if (value instanceof Boolean) {
                v = (Boolean) value;
            } else if (value instanceof String) {
                v = Boolean.parseBoolean((String) value);
            }
            if (v != null) {
                patch.set(key, v);
            }
        };
    }

    private static AttributeSetter integerSetter() {
        return (patch, key, json) -> {
            Object value = json.get(key);
            if (value == null) {
                return;
            }
            Integer v = null;
            if (value instanceof Integer) {
                v = (Integer) value;
            } else if (value instanceof String) {
                try {
                    v = Integer.parseInt((String) value);
                } catch (NumberFormatException ignore) {
                }
            }
            if (v != null) {
                patch.set(key, v);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private static AttributeSetter stringListSetter() {
        return (patch, key, json) -> {
            Object value = json.get(key);
            if (value == null) {
                return;
            }
            List<String> v = null;
            if (value instanceof List) {
                List tmp = (List) value;
                v = new ArrayList<>(tmp.size());
                for (Object el : tmp) {
                    if (el == null) {
                        v.add("");
                    } else if (el instanceof String) {
                        v.add((String) el);
                    } else {
                        v = null;
                        break;
                    }
                }
            } else if (value instanceof String[]) {
                String[] tmp = (String[]) value;
                v = new ArrayList<>(tmp.length);
                for (String el : tmp) {
                    if (el == null) {
                        v.add("");
                    } else {
                        v.add(el);
                    }
                }
            } else if (value instanceof String) {
                v = new ArrayList<>(2);
                v.add((String) value);
            }
            if (v != null) {
                patch.setStrings(key, v);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private static AttributeSetter integerListSetter() {
        return (patch, key, json) -> {
            Object value = json.get(key);
            if (value == null) {
                return;
            }
            List<Integer> v = null;
            if (value instanceof List) {
                List tmp = (List) value;
                v = new ArrayList<>(tmp.size());
                for (Object el : tmp) {
                    if (el == null) {
                        v.add(0);
                    } else if (el instanceof Integer) {
                        v.add((Integer) el);
                    } else {
                        v = null;
                        break;
                    }
                }
            } else if (value instanceof Integer[]) {
                Integer[] tmp = (Integer[]) value;
                v = new ArrayList<>(tmp.length);
                for (Integer el : tmp) {
                    if (el == null) {
                        v.add(0);
                    } else {
                        v.add(el);
                    }
                }
            } else if (value instanceof int[]) {
                int[] tmp = (int[]) value;
                v = new ArrayList<>(tmp.length);
                for (int el : tmp) {
                    v.add(el);
                }
            } else if (value instanceof Integer) {
                v = new ArrayList<>(2);
                v.add((Integer) value);
            }
            if (v != null) {
                patch.setIntegers(key, v);
            }
        };
    }

    private static AttributeGetter getterOf(final Field field) {
        Class<?> clazz = field.getType();
        if (clazz == String.class) {
            return stringGetter(field);
        } else if (clazz == Integer.class || clazz == int.class) {
            return integerGetter(field);
        } else if (clazz == Double.class || clazz == double.class) {
            return doubleGetter(field);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return booleanGetter(field);
        } else if (clazz == Date.class) {
            return dateGetter(field);
        } else if (clazz == TaskStatus.class) {
            return (dst, attr, src)-> {
                Integer v = src.getInteger(attr);
                if (v == null) {
                    return false;
                }
                try {
                    TaskStatus status = TaskStatus.of(v);
                    field.set(dst, status);
                    return true;
                } catch (IllegalAccessException e) {
                    return false;
                }
            };
        } else if (clazz == List.class) {
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            Class<?> elementType = (Class<?>) type.getActualTypeArguments()[0];
            if (elementType == String.class) {
                return stringListGetter(field);
            } else if (elementType == Integer.class || elementType == int.class) {
                return integerListGetter(field);
            }
        } else if (clazz == String[].class) {
            return stringListGetter(field);
        } else if (clazz == int[].class || clazz == Integer[].class) {
            return integerListGetter(field);
        }

        return null;
    }

    private static AttributeGetter stringGetter(final Field field) {
        return (dst, attr, src)-> {
            String v = src.getString(attr);
            if (v == null) {
                return false;
            }
            try {
                field.set(dst, v);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        };
    }

    private static AttributeGetter integerGetter(final Field field) {
        return (dst, attr, src)-> {
            Integer v = src.getInteger(attr);
            if (v == null) {
                return false;
            }
            try {
                field.set(dst, v);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        };
    }

    private static AttributeGetter dateGetter(final Field field) {
        return (dst, attr, src)-> {
            Date v = src.getDate(attr);
            if (v == null) {
                return false;
            }
            try {
                field.set(dst, v);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        };
    }

    private static AttributeGetter stringListGetter(final Field field) {
        return (dst, attr, src)-> {
            List<String> v = src.getStringList(attr);
            if (v == null) {
                return false;
            }
            try {
                field.set(dst, v);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        };
    }

    private static AttributeGetter integerListGetter(final Field field) {
        return (dst, attr, src)-> {
            List<Integer> v = src.getIntegerList(attr);
            if (v == null) {
                return false;
            }
            try {
                field.set(dst, v);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        };
    }

    private static AttributeGetter doubleGetter(final Field field) {
        return (dst, attr, src)-> {
            Double v = src.getDouble(attr);
            if (v == null) {
                return false;
            }
            try {
                field.set(dst, v);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        };
    }

    private static AttributeGetter booleanGetter(final Field field) {
        return (dst, attr, src)-> {
            Boolean v = src.getBoolean(attr);
            if (v == null) {
                return false;
            }
            try {
                field.set(dst, v);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        };
    }

    public static void inflateSavePatch(MultiAttrsObjectPatch patch, JSONObject json) {
        for (Map.Entry<String, AttributeSetter> el : TASK_SETTERS.entrySet()) {
            String attr = el.getKey();
            Object value = json.get(attr);
            if (value == null) {
                continue;
            }
            AttributeSetter setter = el.getValue();
            setter.set(patch, attr, json);
        }
    }

    public Set<String> inflate(MultiAttrsObject object) {
        this.id = object.getId();

        Set<String> attrNames = new HashSet<>(TASK_GETTERS.size());
        for(Map.Entry<String, AttributeGetter> el : TASK_GETTERS.entrySet()) {
            String attr = el.getKey();
            AttributeGetter getter = el.getValue();
            if (getter.get(this, attr, object)) {
                attrNames.add(attr);
            }
        }

        return attrNames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }*/
}
