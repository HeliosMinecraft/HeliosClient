local breakDelaytimer = 0

local function startDuping()
    -- Check if the player is holding an item frame, if yes then dont continue the dupe.
    if mc.player:getMainHandStack():getItem() == Items.ITEM_FRAME then
        return
    end


    -- Get a list of entities within a certain distance from the player
    local entities = PlayerUtils:getEntitiesWithinDistance(mc.player, 150) -- Assuming a distance of 5
    if entities == nil or entities.isEmpty() then
         chatLib.sendHeliosMsg("No entities found within distance.")
        return false, "No entities nearby"
    end
    -- Iterate over the entities
    for i = 1, entities:size() - 1 do
        local itemFrame = entities:get(i)

        -- Check if the entity is an ItemFrameEntity
        if not(itemFrame == nil) and itemFrame:getType() == EntityType.ITEM_FRAME or itemFrame:getType() == EntityType.GLOW_ITEM_FRAME then
            -- The entity is an ItemFrameEntity, you can interact with it here

            -- Look at the itemframe
            RotationUtils:lookAt(itemFrame)

            -- Place the item in hand on the itemframe
            PlayerUtils:doRightClick();

            -- If the item frame has an item, rotate it and then break it after a delay
            if itemFrame:getHeldItemStack():getCount() > 0 then
                -- Rotate the frame
                PlayerUtils:doRightClick();

                -- Delay before attacking the entity
                sleep(1)

                breakDelaytimer = breakDelaytimer + 1

                -- Each click is effectively 1 player tick, so we are going to attack the item-frame after 30 clicks or roughly 1.5 seconds later
                if breakDelaytimer > 15 then
                    PlayerUtils:doLeftClick();
                    breakDelaytimer = 0
                end
                break
            end
        end
    end

    return true, "Success"
end


function onTick()
    return startDuping()
end

function onStop()
    eventManager.unregister("PlayerTick", onTick)
end

function onRun()
    eventManager.register("PlayerTick", onTick)
end

function sleep(sec)
    local t0 = os.clock()
    while os.clock() - t0 <= sec do end
end