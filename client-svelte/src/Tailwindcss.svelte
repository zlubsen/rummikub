<!--https://dev.to/sarioglu/using-svelte-with-tailwindcss-a-better-approach-47ph-->

<style type="text/postcss" global>
    @tailwind base;
    @tailwind components;
    @tailwind utilities;
    body {
        padding:0px;
    }

    tile-panel {
        @apply flex flex-row flex-wrap items-start h-full p-2;
    }
</style>